package com.cocoon.service.impl;

import com.cocoon.dto.InstitutionDTO;
import com.cocoon.entity.payment.Institution;
import com.cocoon.entity.payment.InstitutionResponse;
import com.cocoon.repository.InstitutionsRepo;
import com.cocoon.service.InstitutionService;
import com.cocoon.util.MapperUtil;
import com.cocoon.util.payment.ApiClientUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import yapily.ApiException;
import yapily.sdk.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstitutionServiceImpl implements InstitutionService {

    private final WebClient webClient = WebClient.builder().baseUrl("https://api.yapily.com").build();

    private final InstitutionsRepo institutionsRepo;
    private final MapperUtil mapperUtil;

    public InstitutionServiceImpl(InstitutionsRepo institutionsRepo, MapperUtil mapperUtil) {
        this.institutionsRepo = institutionsRepo;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public List<InstitutionDTO> getAllInstitutions() {
        List<Institution> institutions = institutionsRepo.findAll();
        return institutions.stream().map(obj -> mapperUtil.convert(obj, new InstitutionDTO())).collect(Collectors.toList());
    }

    @Override
    public InstitutionDTO getInstitutionById(String id) {
        Institution institution = institutionsRepo.getById(id);
        return mapperUtil.convert(institution, new InstitutionDTO());
    }

    @Override
    public List<InstitutionDTO> saveIfNotExist(List<String> institutionNames) {

        institutionNames.stream()
                .filter(name -> !name.equalsIgnoreCase(institutionsRepo.findDistinctByName(name).getName()))
                .forEach(name1 -> institutionsRepo.save(new Institution(name1)));

        return institutionsRepo.findAll().stream()
                .map(obj -> mapperUtil.convert(obj, new InstitutionDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionDTO save(InstitutionDTO institutionDTO) {
        Institution institution = mapperUtil.convert(institutionDTO, new Institution());
        return mapperUtil.convert(institutionsRepo.save(institution), new InstitutionDTO());
    }

    @Override
    public List<String> getInstitutionsAtStartUp(){

        List<String> result = new ArrayList<>();
        var response = webClient
                .get()
                .uri("/institutions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Basic ODZlM2ZmZWEtNjFkOC00MTQ5LTk2NmMtM2YzMjFiZWJhYTEyOmZkYTdkZGFlLTE5OWEtNDM3ZS1iMTRkLTI2ZmRjNGI2MmU4Nw==")
                .retrieve()
                .bodyToFlux(InstitutionResponse.class);

        response.toStream()
                .map(InstitutionResponse::getData)
                .forEach(obj -> obj.stream()
                        .map(Institution::getName)
                        .forEach(result::add));

        saveIfNotExist(result);

        return result;
    }

    @Override
    public List<yapily.sdk.Institution> getInstitutionsFromApi() throws ApiException {

        InstitutionsApi institutionsApi = new InstitutionsApi();
        institutionsApi.setApiClient(ApiClientUtils.basicAuth());

        return institutionsApi.getInstitutionsUsingGET("").getData();
    }
}
