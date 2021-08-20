package com.csrc.service;

import com.csrc.dao.ElasticAddressDao;
import com.csrc.model.AddressNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by jianan on 2018/9/10.
 */
@Service
public class AddressService {
    private static final Logger logger= LoggerFactory.getLogger(AddressService.class);

    @Autowired
    private ElasticAddressDao elasticAddressDao;


    public void saveEntity(List<AddressNode> entityList) {
        elasticAddressDao.saveAll(entityList);
    }

    /**
     * 在ES中搜索内容
     */
    public List<AddressNode> searchEntity(String searchContent, Integer level, Integer size) {
//        AddressNode node = new AddressNode();
//        node.setCode("11");
//        node.setRegionLevel(level);
//        node.setFullAddressName(searchContent);
//        Page<AddressNode> pages =  elasticAddressDao.searchSimilar(node,new String[]{"fullAddressName"},PageRequest.of(0,size));
        Page<AddressNode> pages =  elasticAddressDao.findAddressNodeByFullAddressNameLikeAndRegionLevelGreaterThanEqual(searchContent,level,PageRequest.of(0,size));
        return pages.getContent();
    }

    /**
     * 删除es index
     */
    public String deleteIndex(String indexName) {
        elasticAddressDao.deleteAll();
        return "成功";
    }

}
