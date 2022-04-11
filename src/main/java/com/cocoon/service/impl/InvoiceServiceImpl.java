package com.cocoon.service.impl;

import com.cocoon.dto.InvoiceDTO;
import com.cocoon.dto.InvoiceProductDTO;
import com.cocoon.dto.ProfitDTO;
import com.cocoon.dto.UserDTO;
import com.cocoon.entity.Company;
import com.cocoon.entity.Invoice;
import com.cocoon.entity.InvoiceProduct;
import com.cocoon.entity.User;
import com.cocoon.entity.jpa_customization.IInvoiceForDashBoard;
import com.cocoon.enums.InvoiceStatus;
import com.cocoon.enums.InvoiceType;
import com.cocoon.repository.InvoiceProductRepo;
import com.cocoon.repository.InvoiceRepository;
import com.cocoon.service.InvoiceProductService;
import com.cocoon.service.InvoiceService;
import com.cocoon.service.UserService;
import com.cocoon.util.MapperUtil;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final MapperUtil mapperUtil;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceProductRepo invoiceProductRepo;
    private final InvoiceProductService invoiceProductService;
    private final UserService userService;

    @Override
    public InvoiceDTO save(InvoiceDTO dto) {

        Invoice invoice = mapperUtil.convert(dto, new Invoice());
        invoice.setInvoiceStatus(invoice.getInvoiceType().equals(InvoiceType.SALE) ? InvoiceStatus.PENDING : InvoiceStatus.APPROVED);
        invoice.setEnabled((byte) 1);
        invoice.setCompany(getCompanyByLoggedInUser());
        Invoice savedInvoice = invoiceRepository.save(invoice);
        InvoiceDTO savedInvoiceDTO = mapperUtil.convert(savedInvoice, new InvoiceDTO());
        dto.getInvoiceProduct().forEach(obj -> obj.setInvoiceDTO(savedInvoiceDTO));
        invoiceProductService.save(dto.getInvoiceProduct());
        return savedInvoiceDTO;
    }

    @Override
    public InvoiceDTO update(InvoiceDTO dto, Long id) {

        Invoice convertedInvoice = mapperUtil.convert(dto, new Invoice());
        Invoice invoice = invoiceRepository.getById(id);
        convertedInvoice.setInvoiceNumber(invoice.getInvoiceNumber());
        convertedInvoice.setInvoiceStatus(invoice.getInvoiceStatus());
        convertedInvoice.setCompany(invoice.getCompany());
        convertedInvoice.setInvoiceType(invoice.getInvoiceType());
        convertedInvoice.setEnabled(invoice.getEnabled());
        convertedInvoice.setInvoiceDate(invoice.getInvoiceDate());
        Invoice savedInvoice = invoiceRepository.save(convertedInvoice);
        return mapperUtil.convert(savedInvoice, new InvoiceDTO());
    }

    @Override
    public List<InvoiceDTO> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findInvoiceByCompany(getCompanyByLoggedInUser());
        return invoices.stream().map(invoice -> mapperUtil.convert(invoice, new InvoiceDTO())).collect(Collectors.toList());
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.getById(id);
        return mapperUtil.convert(invoice, new InvoiceDTO());
    }

    @Override
    public void deleteInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.getById(id);
        Set<InvoiceProduct> invoiceProducts = invoiceProductRepo.findAllByInvoiceId(invoice.getId());
        invoiceProducts.stream().peek(obj -> obj.setIsDeleted(true)).forEach(invoiceProductRepo::save);
        invoice.setIsDeleted(true);
        invoiceRepository.save(invoice);
    }

    @Override
    public String getInvoiceNumber(InvoiceType invoiceType) {
        List<Invoice> invoiceList = invoiceRepository
                .findInvoicesByCompanyAndInvoiceType(getCompanyByLoggedInUser(), invoiceType)
                .stream()
                .sorted(Comparator.comparing(Invoice::getInvoiceNumber).reversed())
                .collect(Collectors.toList());
        if (invoiceList.size() == 0) {
            if (invoiceType.name().equals("PURCHASE")) return "P-INV001";
            else return "S-INV001";
        }
        int number = Integer.parseInt(invoiceList.get(0).getInvoiceNumber().substring(5)) + 1;
        if (invoiceType.name().equals("PURCHASE")) return "P-INV" + String.format("%03d", number);
        else return "S-INV" + String.format("%03d", number);
    }

    @Override
    public List<InvoiceDTO> getAllInvoicesSorted() {

        List<Invoice> invoices = invoiceRepository.findInvoiceByCompany(getCompanyByLoggedInUser());
        invoices.sort((o2, o1) -> Integer.compare(o2.getInvoiceDate().compareTo(o1.getInvoiceDate()), 0));
        return invoices.stream().limit(3).map(invoice -> mapperUtil.convert(invoice, new InvoiceDTO())).collect(Collectors.toList());

    }

    @Override
    public List<InvoiceDTO> getAllInvoicesByCompanyAndType(InvoiceType type) {
        List<Invoice> invoices = invoiceRepository.findInvoicesByCompanyAndInvoiceType(getCompanyByLoggedInUser(), type);
        return invoices.stream().map(obj -> mapperUtil.convert(obj, new InvoiceDTO())).collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> calculateTotalProfitLoss() {

        Map<String, Integer> map = new HashMap<>();

        List<InvoiceDTO> saleInvoiceDTOS = getAllInvoicesByCompanyAndType(InvoiceType.SALE);

        List<InvoiceDTO> approvedSaleInvoiceDTOS = saleInvoiceDTOS.stream().filter(obj -> obj.getInvoiceStatus() == InvoiceStatus.APPROVED).collect(Collectors.toList());

        List<Set<InvoiceProductDTO>> allSoldInvoiceProducts = approvedSaleInvoiceDTOS.stream().map(obj -> invoiceProductService.getAllInvoiceProductsByInvoiceId(obj.getId())).collect(Collectors.toList());


        List<InvoiceDTO> purchaseInvoices = getAllInvoicesByCompanyAndType(InvoiceType.PURCHASE);
        List<Set<InvoiceProductDTO>> allPurchasedInvoiceProducts = purchaseInvoices.stream().map(obj -> invoiceProductService.getAllInvoiceProductsByInvoiceId(obj.getId())).collect(Collectors.toList());


        int totalIncomeFromSoldProductsWithoutTax = allSoldInvoiceProducts.stream()
                .mapToInt(this::calculateCostWithoutTax)
                .sum();

        int totalIncomeFromSoldProductsWithTax = allSoldInvoiceProducts.stream()
                .mapToInt(this::calculateCostWithTax)
                .sum();

        int totalSpendForPurchasedProductsWithoutTax = allPurchasedInvoiceProducts.stream()
                .mapToInt(this::calculateCostWithoutTax)
                .sum();

        int totalSpendForPurchasedProductsWithTax = allPurchasedInvoiceProducts.stream()
                .mapToInt(this::calculateCostWithTax)
                .sum();

        int totalPurchasedProductQty = allPurchasedInvoiceProducts.stream()
                .mapToInt(this::calculateTotalQty)
                .sum();
        int totalSoldProductQty = allSoldInvoiceProducts.stream()
                .mapToInt(this::calculateTotalQty)
                .sum();
        int eachProductsPurchasedCost = totalSpendForPurchasedProductsWithoutTax / totalPurchasedProductQty;
        int eachProductsSellCost = totalIncomeFromSoldProductsWithoutTax / totalSoldProductQty;
        int eachProductProfit = eachProductsSellCost - eachProductsPurchasedCost;


        int pro =calculateTotalProfit(calculateBuyandSellCostforeachProducts(allSoldInvoiceProducts,allPurchasedInvoiceProducts));

        map.put("totalCost", totalSpendForPurchasedProductsWithTax );
        map.put("totalTax", totalSpendForPurchasedProductsWithTax - totalSpendForPurchasedProductsWithoutTax);
        map.put("totalSales", totalIncomeFromSoldProductsWithTax);
        map.put("totalEarning", pro);

        return map;
    }

    @Override
    public List<IInvoiceForDashBoard> getDashboardInvoiceTop3(Long companyId) {
        List<IInvoiceForDashBoard> invoiceForDashBoards = invoiceRepository.getDashboardInvoiceTop3Interface(companyId, companyId);
        return invoiceForDashBoards;
    }
  
    public List<ProfitDTO> getProfitList() {

        List<InvoiceDTO> saleInvoiceDTOS = getAllInvoicesByCompanyAndType(InvoiceType.SALE);

        List<InvoiceDTO> approvedSaleInvoiceDTOS = saleInvoiceDTOS.stream()
                .filter(obj -> obj.getInvoiceStatus() == InvoiceStatus.APPROVED).collect(Collectors.toList());

        List<Set<InvoiceProductDTO>> allSoldInvoiceProducts = approvedSaleInvoiceDTOS.stream()
                .map(obj -> invoiceProductService.getAllInvoiceProductsByInvoiceId(obj.getId())).collect(Collectors.toList());


        List<InvoiceDTO> purchaseInvoices = getAllInvoicesByCompanyAndType(InvoiceType.PURCHASE);
        List<Set<InvoiceProductDTO>> allPurchasedInvoiceProducts = purchaseInvoices.stream()
                .map(obj -> invoiceProductService.getAllInvoiceProductsByInvoiceId(obj.getId())).collect(Collectors.toList());

        return orderTotalProfitList(calculateBuyandSellCostforeachProducts(allSoldInvoiceProducts,allPurchasedInvoiceProducts));

    }

    private Company getCompanyByLoggedInUser(){
        var currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userDTO = userService.findByEmail(currentUserEmail);
        User user = mapperUtil.convert(userDTO, new User());
        return user.getCompany();
    }

    @Override
    public InvoiceDTO calculateInvoiceCost(InvoiceDTO currentDTO) {

        Set<InvoiceProductDTO> invoiceProducts = invoiceProductService.getAllInvoiceProductsByInvoiceId(currentDTO.getId());
        int costWithoutTax = calculateCostWithoutTax(invoiceProducts);
        currentDTO.setInvoiceCostWithoutTax(costWithoutTax);
        int costWithTax = calculateCostWithTax(invoiceProducts);
        currentDTO.setTotalCost(costWithTax);
        currentDTO.setInvoiceCostWithTax(costWithTax - costWithoutTax);

        return currentDTO;
    }


    public List<ProfitDTO> calculateBuyandSellCostforeachProducts(List<Set<InvoiceProductDTO>> productsSold, List<Set<InvoiceProductDTO>> productsBought) {

        List<ProfitDTO> list=new ArrayList<>();
        int a=0;
        int a1=0;
        int b=0;
        int b1=0;
        int profit = 0;

        for (Set<InvoiceProductDTO> soldList : productsSold) {
            for (InvoiceProductDTO soldProduct : soldList) {
                for (Set<InvoiceProductDTO> boughtList : productsBought) {
                    for (InvoiceProductDTO boughtProduct : boughtList) {
                        if (soldProduct.getName().equals(boughtProduct.getName())&&(soldProduct.getQty()!=0)&&(boughtProduct.getQty()!=0)) {
                            if (soldProduct.getQty() == boughtProduct.getQty()){
                                profit =  (soldProduct.getPrice() - boughtProduct.getPrice()) * soldProduct.getQty();
                                list.add(new ProfitDTO( soldProduct.getName(), soldProduct.getQty(),profit ));
                                soldProduct.setQty(0);
                                boughtProduct.setQty(0);
                            }
                            else if (soldProduct.getQty() < boughtProduct.getQty()) {
                                profit =  (soldProduct.getPrice() - boughtProduct.getPrice()) * soldProduct.getQty();
                                list.add(new ProfitDTO(soldProduct.getName(), soldProduct.getQty(),profit ));
                                boughtProduct.setQty(boughtProduct.getQty() - soldProduct.getQty());
                                soldProduct.setQty(0);
                                a = soldProduct.getQty();
                                a1= boughtProduct.getQty();
                                break;
                            } else if (soldProduct.getQty() > boughtProduct.getQty()) {
                                profit = (soldProduct.getPrice() - boughtProduct.getPrice()) * boughtProduct.getQty();
                                list.add(new ProfitDTO( soldProduct.getName(), soldProduct.getQty(),profit ));
                                soldProduct.setQty(soldProduct.getQty() - boughtProduct.getQty());
                                boughtProduct.setQty(0);
                                b = soldProduct.getQty();
                                b1= boughtProduct.getQty();
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    private int calculateTotalProfit(List<ProfitDTO> list){
        return list.stream().mapToInt(value -> value.getProfit()).sum();
    }

    private List<ProfitDTO> orderTotalProfitList(List<ProfitDTO> list){
        for(int i=0; i< list.size(); i++){
            for (int j=list.size()-1; j!=i;j-- ){
                if(list.get(i).getName().equals(list.get(j).getName())){
                    int k=list.get(i).getProfit()+list.get(j).getProfit();
                    list.get(i).setProfit(list.get(i).getProfit()+list.get(j).getProfit());
                    list.get(i).setQty(list.get(i).getQty()+list.get(j).getQty());
                    list.remove(j);
                   // if ((j>i+1)||(list.size()==3)) j=j-1;
                }
            }
        }
        return list;
    }

    private int calculateCostWithoutTax(Set<InvoiceProductDTO> products) {
        int result = 0;
        for (InvoiceProductDTO product : products) {
            result += (product.getPrice() * product.getQty());
        }
        return result;
    }

    private int calculateCostWithTax(Set<InvoiceProductDTO> products) {
        int result = 0;
        for (InvoiceProductDTO product : products) {
            result += (product.getPrice() * product.getQty()) + (product.getPrice() * product.getQty() * product.getTax() * 0.01);
        }
        return result;
    }


    private int calculateTotalQty(Set<InvoiceProductDTO> products) {
        int result = 0;
        for (InvoiceProductDTO product : products) {
            result += product.getQty();
        }
        return result;
    }

}
