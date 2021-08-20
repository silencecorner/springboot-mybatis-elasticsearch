package com.csrc.dao;

import com.csrc.model.AddressNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticAddressDao extends ElasticsearchRepository<AddressNode,String> {
    Page<AddressNode> findAddressNodeByFullAddressNameLikeAndRegionLevelGreaterThanEqual(String addressName,Integer level, Pageable pageable);
}
