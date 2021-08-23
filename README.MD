# 使用步骤 #
     1.安装elasticsearch，推荐es 5.6，因为版本不同可能部分api访问失败。在配置文件中配置相关地址
     2.执行localhost:7070/address/readAddress 五级地址信息放在resource下，另外，我是500条一执行（因为我给es分的内存只有 256mb，穷啊，买的1g内存的阿里云服务器，内存有限），请根据自己的内存，适当调高，一般本地测的时候，10000条/次导入es无压力。
     3.访问时，localhost:7070/address/searchAddress?addressName=********即可

# 解决方案分析 #


 <div>
    <p><strong>一开始同事的思路：研究分词算法，将用户输入的地址按省市县镇村分隔。</strong></p>
    <p>个人感觉：实现不了，因为用户输入并不是标准输入，例如标准地址为“江苏省南通市海安县李堡镇陈庄村**组**号”，但是用户
    的输入不可控，他可能输入“江苏南通海安....”，也许输入“江苏海安....”，可能多字，可能少字，可能错别字</p>
</div>
<div>
    <p><strong>我的实现思路：分词、倒排索引、文本相似度匹配</strong></p>
    <p>思路听起来很高大上，其实有现成的工具ElasticSearch（底层是Lucene全文搜索引擎）帮我们完成了几乎所有的工作。</p>
    <p><strong>问题分析抽象：</strong></p>
    <p>其实这个场景很清晰，我们就是要从一个地址中识别出各级别，但是这个地址本身不能够为我们提供这些标准数据。<br/>
        转化一下思路，我们可不可以拿用户输入的，去和标准的五级完整数据（例如江苏省南通市海安县李堡镇陈庄村）匹配？取匹配度最高的那一条，标准的那条对应的五级就很清晰明了了。<br/>
        例如用户输入“江苏省海安县李堡镇陈庄村8组88号”，发现和“江苏省南通市海安县李堡镇陈庄村”匹配度最高，而这条文档又可以提供出省、市、县、镇、村相关信息。
    </p>
    <p><strong>付诸实现：详见代码  </strong> </p>
    <br/>
</div>

使用https://github.com/silencecorner/five_addr/tree/txt 抓去数据有67万条
检索时，拿fullAddressName进行匹配，默认带出5条数据，相似度降序排列.例如键入http://localhost:7070/address/searchAddress?addressName=海安李堡陈庄村十一组47号
结果默认展示10条，按相似度降序排列，用的时候一般用第一条，咋用看需求

```json
[
    {
        "fullAddressName": "江苏省南通市海安市李堡镇陈庄村委会",
        "code": "320685103200",
        "parentCode": "320685103",
        "ancestors": "00,32,3206,320685,320685103",
        "name": "陈庄村委会",
        "provinceCode": "32",
        "provinceName": "江苏省",
        "cityCode": "3206",
        "cityName": "南通市",
        "districtCode": "320685",
        "districtName": "海安市",
        "townCode": "320685103",
        "townName": "李堡镇",
        "villageCode": "320685103200",
        "villageName": "陈庄村委会",
        "regionLevel": 5
    },
    {
        "fullAddressName": "天津市天津市静海区独流镇十一堡村委会",
        "code": "120118102218",
        "parentCode": "120118102",
        "ancestors": "00,12,1201,120118,120118102",
        "name": "十一堡村委会",
        "provinceCode": "12",
        "provinceName": "天津市",
        "cityCode": "1201",
        "cityName": "天津市",
        "districtCode": "120118",
        "districtName": "静海区",
        "townCode": "120118102",
        "townName": "独流镇",
        "villageCode": "120118102218",
        "villageName": "十一堡村委会",
        "regionLevel": 5
    },
    {
        "fullAddressName": "江苏省南通市海安市李堡镇新庄村委会",
        "code": "320685103201",
        "parentCode": "320685103",
        "ancestors": "00,32,3206,320685,320685103",
        "name": "新庄村委会",
        "provinceCode": "32",
        "provinceName": "江苏省",
        "cityCode": "3206",
        "cityName": "南通市",
        "districtCode": "320685",
        "districtName": "海安市",
        "townCode": "320685103",
        "townName": "李堡镇",
        "villageCode": "320685103201",
        "villageName": "新庄村委会",
        "regionLevel": 5
    },
    {
        "fullAddressName": "江苏省南通市海安市李堡镇蒋庄村委会",
        "code": "320685103206",
        "parentCode": "320685103",
        "ancestors": "00,32,3206,320685,320685103",
        "name": "蒋庄村委会",
        "provinceCode": "32",
        "provinceName": "江苏省",
        "cityCode": "3206",
        "cityName": "南通市",
        "districtCode": "320685",
        "districtName": "海安市",
        "townCode": "320685103",
        "townName": "李堡镇",
        "villageCode": "320685103206",
        "villageName": "蒋庄村委会",
        "regionLevel": 5
    },
    {
        "fullAddressName": "江苏省南通市海安市李堡镇富庄村委会",
        "code": "320685103213",
        "parentCode": "320685103",
        "ancestors": "00,32,3206,320685,320685103",
        "name": "富庄村委会",
        "provinceCode": "32",
        "provinceName": "江苏省",
        "cityCode": "3206",
        "cityName": "南通市",
        "districtCode": "320685",
        "districtName": "海安市",
        "townCode": "320685103",
        "townName": "李堡镇",
        "villageCode": "320685103213",
        "villageName": "富庄村委会",
        "regionLevel": 5
    }
]
```

```json
PUT /address1
{
    "mappings" : {
      "properties" : {
        "_class" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "ancestors" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "cityCode" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "cityName" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "code" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "districtCode" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "districtName" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "fullAddressName" : {
          "type" : "text",
          "similarity" : "my_bm25",
          "analyzer" : "ik_smart",
          "search_analyzer" : "ik_max_word"
        },
        "name" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "parentCode" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "provinceCode" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "provinceName" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "regionLevel" : {
          "type" : "long"
        },
        "townCode" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "townName" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "villageCode" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "villageName" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        }
      }
    },
   "settings": {
    "similarity": {
      "my_bm25": { 
        "type": "BM25",
        "b":    0 
      }
    }
  }
}
```