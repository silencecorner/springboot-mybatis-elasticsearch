package com.csrc.controller;

import com.csrc.mapper.AddressMapper;
import com.csrc.model.AddressNode;
import com.csrc.service.AddressService;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jianan on 2018/9/3.
 */
@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    AddressService addressService;
    @Autowired
    AddressMapper addressMapper;
    @Value("${address-path:classpath:address.txt}")
    String addressPath;

    @RequestMapping(value="/searchAddress", method= RequestMethod.GET)
    public List<AddressNode> searchWithGet(String addressName,@RequestParam(defaultValue = "3") Integer level,@RequestParam(defaultValue = "5") Integer size) throws IOException {
        List<AddressNode> entityList = null;
        if(StringUtils.isNotEmpty(addressName)) {
            entityList = addressService.searchEntity(addressName,level,size);
            //若返回的十条数据中有三级四级地址，将它们提到前面，底层使用的归并排序，不会破坏三级、四级、五级各自内部初始顺序
            entityList = new ArrayList<>(entityList);
            entityList.sort(Comparator.comparingInt(AddressNode::getRegionLevel));
        }
        return entityList;
    }

    @RequestMapping(value = "/deleteEsIndex",method = RequestMethod.GET)
    public String deleteIndex(String index) {
        return addressService.deleteIndex(index);
    }

    @RequestMapping("/readAddress")
    public void readVillageAddress() throws InterruptedException, FileNotFoundException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("处理开始："+df.format(new Date()));
        File file= ResourceUtils.getFile(addressPath);
        BufferedReader reader=null;
        String temp=null;
        String provinceCode="";
        String cityCode="";
        String districtCode="";
        String townCode="";
        String villageCode="";
        String provinceName="";
        String cityName="";
        String districtName="";
        String townName="";
        String villageName="";

        List<AddressNode> list=new ArrayList<AddressNode>();
        try{
            reader=new BufferedReader(new FileReader(file));
            while((temp=reader.readLine())!=null){
                String[] tempArr=temp.split("@@");
                if(tempArr[4].equals("1")){
                    provinceName=tempArr[3];
                    provinceCode=tempArr[2];
                    AddressNode node=new AddressNode();
                    node.setProvinceName(provinceName);
                    node.setProvinceCode(provinceCode);
                    node.setCode(provinceCode);
                    node.setName(provinceName);
                    node.setParentCode(tempArr[0]);
                    node.setAncestors(tempArr[1]);
                    node.setRegionLevel(1);
                    node.setFullAddressName(provinceName);//组装1级全地址，例如江苏省
                    list.add(node);
                }else if(tempArr[4].equals("2")){
                    cityName=tempArr[3];
                    cityCode=tempArr[2];
                    AddressNode node=new AddressNode();
                    node.setCityName(cityName);
                    node.setCityCode(cityCode);
                    node.setProvinceName(provinceName);
                    node.setProvinceCode(provinceCode);
                    node.setCode(cityCode);
                    node.setName(cityName);
                    node.setParentCode(tempArr[0]);
                    node.setAncestors(tempArr[1]);
                    node.setRegionLevel(2);
                    node.setFullAddressName(provinceName+cityName);//组装2级全地址，例如江苏省南通市
                    list.add(node);
                }else if(tempArr[4].equals("3")){
                    districtName=tempArr[3];
                    districtCode=tempArr[2];

                    AddressNode node=new AddressNode();
                    node.setDistrictName(districtName);
                    node.setDistrictCode(districtCode);
                    node.setCityName(cityName);
                    node.setCityCode(cityCode);
                    node.setProvinceName(provinceName);
                    node.setProvinceCode(provinceCode);
                    node.setCode(districtCode);
                    node.setName(districtCode);
                    node.setParentCode(tempArr[0]);
                    node.setAncestors(tempArr[1]);
                    node.setRegionLevel(3);
                    node.setFullAddressName(provinceName+cityName+districtName);//组装3级全地址，例如江苏省南通市海安县
                    list.add(node);

                }else if(tempArr[4].equals("4")){
                    townName=tempArr[3];
                    townCode=tempArr[2];

                    AddressNode node=new AddressNode();
                    node.setTownName(townName);
                    node.setTownCode(townCode);
                    node.setDistrictName(districtName);
                    node.setDistrictCode(districtCode);
                    node.setCityName(cityName);
                    node.setCityCode(cityCode);
                    node.setProvinceName(provinceName);
                    node.setProvinceCode(provinceCode);
                    node.setCode(townCode);
                    node.setName(townName);
                    node.setParentCode(tempArr[0]);
                    node.setAncestors(tempArr[1]);
                    node.setRegionLevel(4);
                    node.setFullAddressName(provinceName+cityName+districtName+townName);//组装4级全地址，例如江苏省南通市海安县李堡镇
                    list.add(node);
                }else {
                    villageName=tempArr[3];
                    villageCode=tempArr[2];
                    AddressNode node=new AddressNode();
                    node.setVillageName(villageName);
                    node.setVillageCode(villageCode);
                    node.setTownName(townName);
                    node.setTownCode(townCode);
                    node.setDistrictName(districtName);
                    node.setDistrictCode(districtCode);
                    node.setCityName(cityName);
                    node.setCityCode(cityCode);
                    node.setProvinceName(provinceName);
                    node.setProvinceCode(provinceCode);
                    node.setCode(villageCode);
                    node.setName(villageName);
                    node.setParentCode(tempArr[0]);
                    node.setAncestors(tempArr[1]);
                    node.setRegionLevel(5);
                    node.setFullAddressName(provinceName+cityName+districtName+townName+villageName);//数据库中存储的是5级全地址
                    list.add(node);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(reader!=null){
                try{
                    reader.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        System.out.println("处理完成，共有多少数据："+list.size());
        int lineNum=0;
        System.out.println("开始分段执行");
        while(lineNum<list.size()){
            System.out.println("开始linNum:"+lineNum);
            List<AddressNode> listTemp=list.subList(lineNum,(list.size()-lineNum>500)?lineNum+500:list.size());
            addressMapper.importIntoDb(listTemp);//存入结构化数据库中，但是暂时用不到
            addressService.saveEntity(listTemp);//存入es中
            System.out.println("结束linNum:"+lineNum);
            lineNum+=500;//分段执行，以免outOfMemory
            Thread.sleep(500);//给es或数据库一个缓冲时间
        }
        System.out.println("处理结束："+df.format(new Date()));
    }
}
