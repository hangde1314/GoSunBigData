package com.hzgc.common.service.rest;

/**
 * 大数据接口请求路径
 */
public class BigDataPath {

    /**
     * 大数据路径
     */
    private static final String ROOT = "";

    /**
     * Collect模块请求路径
     */
    public static final String FTP_GET_PROPERTIES = "/ftp_info";
    public static final String FTP_GET_IP = "/hostname_convert";
    public static final String FTP_SUBSCRIPTION_OPEN = "/subscribe_open";
    public static final String FTP_SUBSCRIPTION_CLOSE = "/subscribe_close";
    public static final String HTTP_HOSTNAME_TO_IP = "/http_hostname_to_ip";
    public static final String HOSTNAME_TO_IP = "/hostname_to_ip";

    public static final String FEATURE_EXTRACT = "/extract_feature";
    public static final String FEATURE_EXTRACT_BIN = "/extract_bin";
    public static final String FACE_FEATURE_EXTRACT_BASE64 = "/face_extract_base64";
    public static final String FEATURE_EXTRACT_BASE64 = "extract_base64";
    public static final String FEATURE_CHECK_BASE64 = "/check_base64";
    public static final String CAR_FEATURE_CHECK_BASE64 = "/car_check_base64";
    public static final String PERSON_FEATURE_CHECK_BASE64 = "/person_check_base64";

    public static final String FEATURE_EXTRACT_FTP = "/extract_ftp";
    public static final String FACE_ATTRIBUTE = "/attribute";
    public static final String FEATURE_EXTRACT_BYTES = "/extract_bytes";
    public static final String CAR_ATTRIBUTE = "/car_attribute";
    public static final String CAR_CAPTURE = "/car_capture";
    public static final String CAR_EXTRACT = "/car_extract";

    /**
     * People模块请求路径
     */
    public static final String PEOPLE_EDULEVEL = "/people_edulevel";
    public static final String PEOPLE_FLAG = "/people_flag";
    public static final String PEOPLE_POLITIC = "/people_politic";
    public static final String PEOPLE_PROVINCES = "/people_provinces";
    public static final String PEOPLE_CITY = "/people_city";
    public static final String PEOPLE_INSERT="/people_insert";
    public static final String PEOPLE_UPDATE="/people_update";
    public static final String PEOPLE_SELECT_BY_PEOPLEID="/select_peopleid";
    public static final String PEOPLE_SEARCH_PICTURE_BY_IDCARD="/select_idcard";
    public static final String PEOPLE_SEARCH_PICTURE_BY_PICID="/search_picture_picid";
    public static final String PEOPLE_SEARCH_PICTURE_BY_PEOPLEID="/search_picture_peopleid";
    public static final String PEOPLE_SELECT_PEOPLE="/search_people";
    public static final String PEOPLE_SELECT_COMMUNITY="/search_community";
    public static final String PEOPLE_EXCEL_IMPORT = "/excel_import";

    public static final String COMMUNITY_COUNT = "/count_people";
    public static final String COMMUNITY_PEOPLE = "/people";
    public static final String COMMUNITY_PEOPLE_IMPORTANT = "/people_important";
    public static final String COMMUNITY_PEOPLE_CARE = "/people_care";
    public static final String COMMUNITY_PEOPLE_NEW = "/people_new";
    public static final String COMMUNITY_PEOPLE_OUT = "/people_out";
    public static final String COMMUNITY_COUNT_NEW_OUT = "/count_new_out";
    public static final String COMMUNITY_SEARCH_NEW_OUT = "/search_new_out";
    public static final String COMMUNITY_PEOPLE_INFO = "/community_people_info";
    public static final String COMMUNITY_PEOPLE_INFO_IDCARD="/search_people_idcard";
    public static final String COMMUNITY_SEARCH_OUT_LAST_CAPTURE = "/out_last_capture";
    public static final String COMMUNITY_SEARCH_NEW_CAPTURE = "/new_capture";
    public static final String COMMUNITY_AFFIRM_NEW = "/affirm_new";
    public static final String COMMUNITY_AFFIRM_NEW_HANDLE = "/affirm_new_handle";
    public static final String COMMUNITY_AFFIRM_OUT = "/affirm_out";
    public static final String COMMUNITY_PEOPLE_CAPTURE_1MONTH = "/search_capture";
    public static final String COMMUNITY_PEOPLE_DEVICE_TRACK_1MONTH = "/device_track";
    public static final String COMMUNITY_PEOPLE_DEVICE_CAPTURE_1MONTH = "/device_capture";
    public static final String COMMUNITY_PEOPLE_CAPTURE_3MONTH = "/count_capture";
    public static final String COMMUNITY_IMPORTANT_PEOPLE_RECOGNIZE = "/important_recognize";

    /**
     * Clustering模块请求路径
     */
    public static final String PEOPLEIN_SEARCH = "/peoplein_search";
    public static final String PEOPLEIN_TOTLE = "/peoplein_totle";
    public static final String PEOPLEIN_DETAILSEARCH_V1 = "/peoplein_detail";
    public static final String PEOPLEIN_DELETE = "/peoplein_delete";
    public static final String PEOPLEIN_IGNORE = "/peoplein_ignore";
    public static final String PEOPLEIN_MOVEIN="/peoplein_movein";
    public static final String PEOPLEIN_LOCUS="/peoplein_locus";

    public static final String PEOPLEMANAGER_SAVEPLAN ="/peoplemanager_saveplan";
    public static final String PEOPLEMANAGER_SEARCHPLAN = "/peoplemanager_searchplan";
    public static final String PEOPLEMANAGER_MODIFYPLAN = "/peoplemanager_modifyplan";
    public static final String PEOPLEMANAGER_DELETEPLAN = "/peoplemanager_deleteplan";
    public static final String PEOPLEMANAGER_ADDPERSON="/peoplemanager_addperson";
    public static final String PEOPLEMANAGER_DELETEPERSON="/peoplemanager_deleteperson";
    public static final String PEOPLEMANAGER_MODIFYPERSON="/peoplemanager_modifyperson";
    public static final String PEOPLEMANAGER_GETPERSON="/peoplemanager_getperson";
    public static final String PEOPLEMANAGER_PERSONSEARCH="/peoplemanager_personsearch";;
    public static final String PEOPLEMANAGER_CAPTURECOUNT="/peoplemanager_capturecount";
    public static final String PEOPLEMANAGER_CAPTUREHISTORY="/peoplemanager_capturehistory";
    public static final String PEOPLEMANAGER_RESIDENTSEARCH="/peoplemanager_residentsearch";
    public static final String PEOPLEMANAGER_GETRESIDENTPICTURE="/peoplemanager_getresidentpicture";
    public static final String PEOPLEMANAGER_CAPTURELOCUS="/peoplemanager_capturelocus";

    /**
     * FaceDynrepo模块请求路径
     */
    public static final String DYNREPO_SEARCH = "/search_picture";
    public static final String DYNREPO_SEARCHRESULT = "/search_result";
    public static final String DYNREPO_GETPICTURE = "/origin_picture";
    public static final String DYNREPO_HISTORY = "/capture_history";
    public static final String CAR_DYNREPO_HISTORY = "/car_capture_history";
    public static final String DYNREPO_SEARCHHISTORY="/search_history";
    public static final String DYNREPO_CAPTURE_LASTTIME="/capture_last_time";

    /**
     * visual模块请求路径
     */
    public static final String CAPTURECOUNT_DYNREPO = "/capture_day_count";
    public static final String CAPTURECOUNT_IPCIDS_TIME = "/face_hours";
    public static final String CAPTURECOUNT_SIX_HOUR = "/sixhours_count";
    public static final String CAPTURECOUNT_IPCIDS = "/face";
    public static final String PEOPLE_COUNT="/people_count";
    public static final String GET_PICTURE = "/image";
    public static final String GET_CARE_PEOPLE = "/get_care_people";
    public static final String GET_STATUS_PEOPLE = "/get_status_people";
    public static final String GET_IMPORTANT_PEOPLE = "/get_important_people";

    /**
     * Dispatch模块请求路径
     */
    public static final String DISPATCH_ADD = "/add_rule";
    public static final String DISPATCH_MODIFY = "/modify_rule";
    public static final String DISPATCH_DELETE = "/delete_rules";
    public static final String DISPATCH_SEARCH_BYID = "/rule_info";
    public static final String DISPATCH_CUTPAGE_RULE = "/get_rule";
    public static final String DISPATCH_DISPATCH_STATUS = "/dispatch_status";
    public static final String DISPATCH_GET_FACE = "/get_face";
    public static final String DISPATCH_SEARCH_DISPATCH = "/search_dispatch";
    public static final String DISPATCH_INSERT_DEPLOY = "/insert";
    public static final String DISPATCH_DELETE_DEPLOY = "/delete";
    public static final String DISPATCH_UPDATE_DEPLOY = "/update";
    public static final String DISPATCH_SEARCH_HISTORY = "/search_recognize";
    public static final String DISPATCH_EXCEL_IMPORT = "/excel_import";
    public static final String DISPATCH_INSERT_WHITE = "/insert_white";
    public static final String DISPATCH_DELETE_WHITE = "/delete_white";
    public static final String DISPATCH_UPDATE_WHITE = "/update_white";
    public static final String DISPATCH_WHITE_STATUS = "/white_status";
    public static final String DISPATCH_SEARCH_WHITE = "/search_white";
    public static final String DISPATCH_INSERT_ALIVE = "/insert_alive";
    public static final String DISPATCH_DELETE_ALIVE = "/delete_alive";
    public static final String DISPATCH_UPDATE_ALIVE = "/update_alive";
    public static final String DISPATCH_ALIVE_STATUS = "/alive_status";
    public static final String DISPATCH_SEARCH_ALIVE = "/search_alive";
    public static final String DISPATCH_EXCEL_TEMPLATE = "/template";


    public static final String OBJECTINFO_ADD = "/object_add";
    public static final String OBJECTINFO_DELETE = "/object_delete";
    public static final String OBJECTINFO_UPDATE = "/object_update";
    public static final String OBJECTINFO_UPDATE_STATUS = "/object_update_status";
    public static final String OBJECTINFO_GET = "/object_get";
    public static final String OBJECTINFO_GET_CARE_PEOPLE = "/get_care_people";
    public static final String OBJECTINFO_GET_STATUS_PEOPLE = "/get_status_people";
    public static final String OBJECTINFO_GET_IMPORTANT_PEOPLE = "/get_important_people";
    public static final String OBJECTINFO_SEARCH = "/object_search";
    public static final String OBJECTINFO_GET_PHOTOBYKEY = "/get_object_photo";
    public static final String OBJECTINFO_GET_FEATURE = "/get_feature";
    public static final String STAREPO_GET_SEARCHRESULT= "/get_search_result";
    public static final String STAREPO_GET_SEARCHPHOTO = "/get_search_photo";
    public static final String STAREPO_CREATE_WORD = "/create_word";
    public static final String STAREPO_EXPORT_WORD = "/export_word";
    public static final String OBJECTINFO_COUNT_STATUS = "/count_status";
    public static final String STAREPO_COUNT_EMIGRATION = "/count_emigration";

    public static final String TYPE_ADD = "/type_add";
    public static final String TYPE_DELETE = "/type_delete";
    public static final String TYPE_UPDATE = "/type_update";
    public static final String TYPE_SEARCH = "/type_search";
    public static final String TYPE_SEARCH_NAMES = "/type_search_names";

    /**
     *VehicleDynrepo模块请求路径
     */
    public static final String DYNCAR_CAPTURE_HISTORY = "/vehicle_history";

    /**
     * PersonDynrepo模块请求路径
     */
    public static final String PERSON_ATTRIBUTE= "/person_attribute" ;
    public static final String CAPTURE_HISTORY= "/person_capture_history" ;
    public static final String PERSON_FEATURE_EXTRACT_BIN = "/person_extract_bin";

    /**
     * ImsiDynrepo模块请求路径
     */
    public static final String IMSI_SEARCH_BY_TIME = "/query_by_time" ;
    public static final String MAC_SEARCH_BY_SNS = "query_by_sns";

}