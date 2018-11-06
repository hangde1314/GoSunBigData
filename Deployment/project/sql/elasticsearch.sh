#!/bin/bash
## 动态信息库person 表的映射

## 删除索引
##curl -XDELETE 's100:9200/dynamicface?pretty'  -H 'Content-Type: application/json'
##curl -XDELETE 's100:9200/dynamicperson?pretty' -H 'Content-Type:application/json'
##curl -XDELETE 's100:9200/dynamiccar?pretty'  -H 'Content-Type: application/json'

curl -XPUT 's100:9200/dynamicface?pretty' -H 'Content-Type: application/json' -d'
{
    "settings": {
	    "number_of_shards":5,
        "number_of_replicas":2,
        "analysis": {
            "filter": {
                "trigrams_filter": {
                    "type":     "ngram",
                    "min_gram": 2,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "trigrams": {
                    "type":      "custom",
                    "tokenizer": "standard",
                    "filter":   [
                        "lowercase",
                        "trigrams_filter"
                    ]
                },
                "ik": {
                    "tokenizer" : "ik_max_word"
                }
            }
        }
    },
    "mappings": {
         "face": {
         		"properties": {
         			"sftpurl": {
                        "type": "keyword"
                    },
                    "bftpurl": {
                    	"type": "keyword"
                    },
         			"eyeglasses": {
         				"type": "keyword"
         			},
         			"age": {
                         "type": "keyword"
                    },
         			"mask": {
                    	"type": "keyword"
                    },
         			"gender": {
         				"type": "keyword"
         			},
         			"huzi": {
         				"type": "keyword"
         			},
         			"sharpness": {
         			    "type": "keyword"
         			},
         			"feature": {
         			    "type": "keyword"
         			},
         			"bitfeature": {
         			    "type": "keyword"
         			},
         			"ipcid": {
         				"type": "keyword"
         			},
         			"timestamp": {
         				"type": "date",
         				"format": "yyyy-MM-dd HH:mm:ss"
         			},
         			"hostname": {
         			    "type": "keyword"
         			},
         			"sabsolutepath": {
                    	"type": "keyword"
                    },
                    "babsolutepath": {
                        "type": "keyword"
                    }
         		}
         	}
        }
    }'

curl -XPUT 's100:9200/dynamicface/_settings' -d '{
    "index": {
        "max_result_window": 1000000000
    }
}'

##创建索引--行人
curl -XPUT 's100:9200/dynamicperson?pretty' -H 'Content-Type: application/json' -d'
{
    "settings": {
	    "number_of_shards":5,
        "number_of_replicas":2,
        "analysis": {
            "filter": {
                "trigrams_filter": {
                    "type": "ngram",
                    "min_gram": 2,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "trigrams": {
                    "type":      "custom",
                    "tokenizer": "standard",
                    "filter":   [
                        "lowercase",
                        "trigrams_filter"
                    ]
                }
            }
        }
    },
    "mappings": {
         "person": {
         		"properties": {
         			"age": {
         				"type": "keyword"
         			},
         			"baby": {
         				"type": "keyword"
         			},
         			"bag": {
         				"type": "keyword"
         			},
         			"bottomcolor": {
         				"type": "keyword"
         			},
         			"bottomtype": {
         				"type": "keyword"
         			},
         			"hat": {
         				"type": "keyword"
         			},
         			"hair": {
         				"type": "keyword"
         			},
         			"knapsack": {
         				"type": "keyword"
         			},
         			"messengerbag": {
         				"type": "keyword"
         			},
         			"orientation": {
         				"type": "keyword"
         			},
         			"sex": {
         				"type": "keyword"
         			},
         			"shoulderbag": {
         				"type": "keyword"
         			},
         			"umbrella": {
         				"type": "keyword"
         			},
         			"uppercolor": {
         				"type": "keyword"
         			},
         			"uppertype": {
                    	"type": "keyword"
                    },
					"cartype": {
						"type": "keyword"
					},
					"feature": {
                        "type": "keyword"
                    },
                    "bitfeature": {
                        "type": "keyword"
                    },
					"ipcid": {
						"type": "keyword"
					},
					"timestamp": {
						"type": "date",
						"format": "yyyy-MM-dd HH:mm:ss"
					},
					"sftpurl": {
						"type": "keyword"
					},
					"bftpurl": {
						"type": "keyword"
					},
					"hostname": {
						"type": "keyword"
					},
					"sabsolutepath": {
						"type": "keyword"
					},
					"babsolutepath": {
                    	"type": "keyword"
                    }
         		}
         	}
        }
    }'

curl -XPUT 's100:9200/dynamicperson/_settings' -d '{
     "index": {
        "max_result_window": 1000000000
    }
}'

## 创建索引--车辆
curl -XPUT 's100:9200/dynamiccar?pretty' -H 'Content-Type: application/json' -d'
{
    "settings": {
	    "number_of_shards":5,
        "number_of_replicas":2,
        "analysis": {
            "analyzer": {
                "ik": {
                    "tokenizer" : "ik_smart"
                }
            }
        }
    },
    "mappings": {
         "car": {
         		"properties": {
         			"ipcid": {
         				"type": "keyword"
         			},
         			"timestamp": {
         				"type": "date",
                        "format": "yyyy-MM-dd HH:mm:ss"
         			},
         			"sftpurl": {
         				"type": "keyword"
         			},
         			"bftpurl": {
         				"type": "keyword"
         			},
         			"sabsolutepath": {
         				"type": "keyword"
         			},
         			"babsolutepath": {
         				"type": "keyword"
         			},
         			"hostname": {
         				"type": "keyword"
         			},
         			"feature": {
                        "type": "keyword"
                    },
                    "bitfeature": {
                        "type": "keyword"
                    },
         			"vehicle_object_type": {
         				"type": "keyword"
         			},
         			"belt_maindriver": {
         				"type": "keyword"
         			},
         			"belt_codriver": {
         				"type": "keyword"
         			},
					"brand_name": {
         				"type": "text",
         				"analyzer": "ik_smart",
         				"search_analyzer": "ik_smart"
         			},
         			"call_code": {
                    	"type": "keyword"
                    },
					"vehicle_color": {
                    	"type": "keyword"
                    },
					"crash_code": {
                    	"type": "keyword"
                    },
					"danger_code": {
                    	"type": "keyword"
                    },
					"marker_code": {
                    	"type": "keyword"
                    },
					"plate_schelter_code": {
                    	"type": "keyword"
                    },
					"plate_flag_code": {
						"type": "keyword"
					},
					"plate_licence": {
						"type": "keyword"
					},
					"plate_destain_code": {
						"type": "keyword"
					},
					"plate_color_code": {
						"type": "keyword"
					},
					"plate_type_code": {
						"type": "keyword"
					},
					"rack_code": {
						"type": "keyword"
					},
					"sunroof_code": {
						"type": "keyword"
					},
					"vehicle_type": {
						"type": "keyword"
					}
         		}
         	}
        }
    }'

curl -XPUT 's100:9200/dynamiccar/_settings' -d '{
     "index": {
        "max_result_window": 1000000000
    }
}'