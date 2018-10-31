

object Config {

    //Global Config
    val USERNAME : String = "tpcds"
    val PASSWORD : String = "TPCds2018"
    val DB : String = "tpcds"
    val URL : String = "jdbc:mysql://localhost:3306/" + DB

    //Table names
    val CALL_CENTER : String = "call_center"
    val CATALOG_PAGE : String = "catalog_page"
    val CATALOG_RETURNS : String = "catalog_returns"
    val CATALOG_SALES : String = "catalog_sales"
    val CUSTOMER : String = "customer"
    val CUSTOMER_ADDRESS : String = "customer_address"
    val CUSTOMER_DEMOGRAPHICS : String = "customer_demographics"
    val DATE_DIM : String = "date_dim"
    val DBGEN_VERSION : String = "dbgen_version"
    val HOUSEHOLD_DEMOGRAPHICS : String = "household_demographics"
    val INCOME_BAND : String = "income_band"
    val INVENTORY : String = "inventory"
    val ITEM : String = "item"
    val PROMOTION : String = "promotion"
    val REASON : String = "reason"
    val SHIP_MODE : String = "ship_mode"
    val STORE : String = "store"
    val STORE_RETURNS : String = "store_returns"
    val STORE_SALES : String = "store_sales"
    val TIME_DIM : String = "time_dim"
    val WAREHOUSE : String = "warehouse"
    val WEB_PAGE : String = "web_page"
    val WEB_RETURNS : String = "web_returns"
    val WEB_SALES : String = "web_sales"
    val WEB_SITE : String = "web_site"

    //Partitioning Keys
    val CATALOG_SALES_PARTITIONING_KEY = "cs_item_sk"
    val CUSTOMER_PARTITIONING_KEY = "c_customer_sk"
    val CUSTOMER_DEMOGRAPHICS_PARTITIONING_KEY = "cd_demo_sk"
    val CUSTOMER_ADDRESSES_PARTITIONING_KEY = "ca_address_sk"
    val STORE_RETURNS_PARTITIONING_KEY = "sr_item_sk"
    val STORES_SALES_PARTITIONING_KEY = "ss_item_sk"
    val INVENTORY_PARTITIONING_KEY = "inv_item_sk"
    val WEB_SALES_PARTITIONING_KEY = "ws_item_sk"
    val WEB_RETURNS_PARTITIONING_KEY = "wr_item_sk"
    val DATE_DIM_PARTITIONING_KEY = "d_date_sk"
    val ITEM_PARTITIONING_KEY = "i_item_sk"
    val TIME_PARTITIONING_KEY = "t_time_sk"

}
