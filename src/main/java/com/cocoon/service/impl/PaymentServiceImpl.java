package com.cocoon.service.impl;

import com.cocoon.dto.CompanyDTO;
import com.cocoon.dto.PaymentDTO;
import com.cocoon.entity.Company;
import com.cocoon.entity.Payment;
import com.cocoon.entity.payment.Institution;
import com.cocoon.enums.Months;
import com.cocoon.repository.PaymentRepository;
import com.cocoon.service.CompanyService;
import com.cocoon.service.PaymentService;
import com.cocoon.util.MapperUtil;
import com.cocoon.util.payment.ApiClientUtils;
import com.cocoon.util.payment.Constants;
import com.cocoon.util.payment.PaymentRequestUtils;
import com.cocoon.util.payment.UserUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import yapily.ApiClient;
import yapily.ApiException;
import yapily.sdk.*;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MapperUtil mapperUtil;
    private final CompanyService companyService;

    public PaymentServiceImpl( PaymentRepository paymentRepository, MapperUtil mapperUtil, CompanyService companyService) {
        this.paymentRepository = paymentRepository;
        this.mapperUtil = mapperUtil;
        this.companyService = companyService;
    }

    @Override
    public List<PaymentDTO> getAllPaymentsByYear(int year) {

        LocalDate instance = LocalDate.now().withYear(year);
        LocalDate dateStart = instance.with(firstDayOfYear());
        LocalDate dateEnd = instance.with(lastDayOfYear());
        var company = companyService.getCompanyByLoggedInUser();

        List<Payment> payments = paymentRepository.findAllByYearBetweenAndCompanyId(dateStart, dateEnd, company.getId());
        return payments.stream().map(obj -> mapperUtil.convert(obj, new PaymentDTO())).collect(Collectors.toList());
    }

    @Override
    public void createPaymentsIfNotExist(int year) {

        LocalDate instance = LocalDate.now().withYear(year);
        LocalDate dateStart = instance.with(firstDayOfYear());
        LocalDate dateEnd = instance.with(lastDayOfYear());
        CompanyDTO companyDto = companyService.getCompanyByLoggedInUser();

        List<Payment> payments = paymentRepository.findAllByYearBetweenAndCompanyId(dateStart, dateEnd, companyDto.getId());


        if (payments.size() == 0) {
            for (Months month : Months.values()){
                Payment payment = new Payment();
                payment.setMonth(month);
                payment.setYear(LocalDate.now().withYear(year));
                payment.setPaid(false);
                payment.setAmount(250);
                payment.setCompany(mapperUtil.convert(companyDto, new Company()));
                paymentRepository.save(payment);
            }
        }

    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.getById(id);
        return mapperUtil.convert(payment, new PaymentDTO());
    }

    @Override
    public PaymentDTO updatePayment(PaymentDTO paymentDTO) {

        Payment payment = paymentRepository.getById(paymentDTO.getId());
        payment.setInstitution(mapperUtil.convert(paymentDTO.getInstitution(), new Institution()));
        payment.setPaid(true);
        return mapperUtil.convert(paymentRepository.save(payment), new PaymentDTO());
    }

    @Override
    public void makePaymentWithSelectedInstitution(String institutionId) throws ApiException, URISyntaxException {

        ApiClient defaultClient = ApiClientUtils.basicAuth();

        System.out.println("Configured application credentials for API: " + defaultClient.getBasePath());

        final ApplicationUsersApi usersApi = new ApplicationUsersApi();

        ApplicationUser applicationUser = UserUtils.createOrUseExistingApplciationUser(Constants.APPLICATION_USER_ID, defaultClient);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("Using user:");
        System.out.println(gson.toJson(applicationUser));

        PaymentsApi paymentsApi = new PaymentsApi(defaultClient);

        // Create a new payment authorisation request
        PaymentAuthorisationRequest paymentAuthorisationRequest = new PaymentAuthorisationRequest();
        paymentAuthorisationRequest.setApplicationUserId(Constants.APPLICATION_USER_ID);
        paymentAuthorisationRequest.setInstitutionId(institutionId);

        // Create the payment request detailing the payment to attach to the authorisation request
        PaymentRequest paymentRequest = PaymentRequestUtils.createNewDomesticPaymentRequestWithSortCodeAndAccountNumber(
                new BigDecimal(250),
                "GBP",
                "Domestic Payment",
                UUID.randomUUID().toString().substring(0,30), // anyUniqueStringOver18characters
                "Cocoon Accounting",
                "700001",
                "70000005"
        );
        paymentAuthorisationRequest.setPaymentRequest(paymentRequest);
        System.out.println();
        System.out.println("Sending a new payment authorisation request: ");
        System.out.println(gson.toJson(paymentAuthorisationRequest));

        // Send the payment authorisation request
        ApiResponseOfPaymentAuthorisationRequestResponse authorizationResponse = paymentsApi.createPaymentAuthorisationUsingPOST(paymentAuthorisationRequest, "", "", "", "");

        URI url = new URI(authorizationResponse.getData().getAuthorisationUrl());

        if (Desktop.isDesktopSupported()) {
            try {
                System.out.println("Opening browser with auth url.");
                Desktop.getDesktop().browse(url);

                // After authentication, you should be redirected to a static page that can be closed
                System.out.println("After completing authentication, press Enter to continue: [enter]");
                // to implement with user input
                //System.in.read();
                // to implement WITHOUT user input
                Thread.sleep(10000);

                // Get user consents
                final ConsentsApi consentsApi = new ConsentsApi(defaultClient);

                System.out.println("Obtaining the most recent consent filtered by application user Id [" +
                        Constants.APPLICATION_USER_ID + "] and institution [" + institutionId + "] with GET /consents?" +
                        "filter[applicationUserId]=" + Constants.APPLICATION_USER_ID + "&filter[institution]=" + institutionId);
                System.out.println("Validating that the consent is AUTHORIZED");

                Consent consent = consentsApi.getConsentsUsingGET(
                                null,
                                Collections.singletonList(Constants.APPLICATION_USER_ID),
                                Collections.emptyList(),
                                Collections.singletonList(institutionId),
                                Collections.emptyList(),
                                null,
                                null,
                                1,
                                null).getData().stream()
                        .filter(c -> c.getStatus().equals(Consent.StatusEnum.AUTHORIZED))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(String.format("No consent token present for application user %s", Constants.APPLICATION_USER_ID)));

                final String consentToken = consent.getConsentToken();
                // to get the consent token
                //System.out.println("Consent Token is " + consentToken);

                // Create the payment with the same payment request object used in the payment authorisation request
                ApiResponseOfPaymentResponse response = paymentsApi.createPaymentUsingPOST(consentToken, paymentRequest, "", "", "", "");

                System.out.println("Payment submitted");

                PaymentResponse.StatusEnum status = response.getData().getStatus();
                // to get the payment id
                //System.out.println("Payment Response Id = " + response.getData().getId());

                while (status == PaymentResponse.StatusEnum.PENDING) {
                    ApiResponseOfPaymentResponse apiResponseOfPaymentResponse = paymentsApi.getPaymentStatusUsingGET(response.getData().getId(), consentToken, "", "", "", "");
                    status = apiResponseOfPaymentResponse.getData().getStatus();
                    Thread.sleep(1000);
                }

                System.out.println("Payment was executed with status: " + status);

            } catch (final IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                // After authentication, you should be redirected to a static page that can be closed
                System.out.println("After completing authentication, press Enter to continue: [enter]");
                // to implement with user input
                System.in.read();
                // to implement WITHOUT user input
                //Thread.sleep(12000);

                // Get user consents
                final ConsentsApi consentsApi = new ConsentsApi(defaultClient);

                System.out.println("Obtaining the most recent consent filtered by application user Id [" +
                        Constants.APPLICATION_USER_ID + "] and institution [" + institutionId + "] with GET /consents?" +
                        "filter[applicationUserId]=" + Constants.APPLICATION_USER_ID + "&filter[institution]=" + institutionId);
                System.out.println("Validating that the consent is AUTHORIZED");

                Consent consent = consentsApi.getConsentsUsingGET(
                                null,
                                Collections.singletonList(Constants.APPLICATION_USER_ID),
                                Collections.emptyList(),
                                Collections.singletonList(institutionId),
                                Collections.emptyList(),
                                null,
                                null,
                                1,
                                null).getData().stream()
                        .filter(c -> c.getStatus().equals(Consent.StatusEnum.AUTHORIZED))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(String.format("No consent token present for application user %s", Constants.APPLICATION_USER_ID)));


                final String consentToken = consent.getConsentToken();
                // to get the consent token
                System.out.println("Consent Token is " + consentToken);

                // Create the payment with the same payment request object used in the payment authorisation request
                ApiResponseOfPaymentResponse response = paymentsApi.createPaymentUsingPOST(consentToken, paymentRequest, "", "", "", "");

                System.out.println("Payment submitted");

                PaymentResponse.StatusEnum status = response.getData().getStatus();
                // to get the payment id
                System.out.println("Payment Response Id = " + response.getData().getId());

                while (status == PaymentResponse.StatusEnum.PENDING) {
                    ApiResponseOfPaymentResponse apiResponseOfPaymentResponse = paymentsApi.getPaymentStatusUsingGET(response.getData().getId(), consentToken, "", "", "", "");
                    status = apiResponseOfPaymentResponse.getData().getStatus();
                    Thread.sleep(1000);
                }

                System.out.println("Payment was executed with status: " + status);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
