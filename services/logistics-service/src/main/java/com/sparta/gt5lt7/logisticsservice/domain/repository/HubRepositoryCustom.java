package com.sparta.gt5lt7.logisticsservice.domain.repository;

import com.sparta.gt5lt7.logisticsservice.domain.entity.Hub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepositoryCustom {

    Page<Hub> searchHubs(String keyword, Pageable pageable);
}