package jp.enbind.mcp.service;

import org.springframework.ai.tool.annotation.Tool;

public class SimpleService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleService.class);

    @Tool(description = "現在の日付を取得します。今日や現在時刻などの情報を求められたときに値を提供します")
    public String getCurrentDate(){
        String date = java.time.LocalDate.now().toString();
        log.info("getCurrentDate invoked : {}",date);
        return date;
    }

    @Tool(description = "与えられた日付(YYYY-MM-DD形式)から、天気情報を提供します")
    public String getWeatherInfo(String date){
        log.info("getWeatherInfo invoked : input({})",date);
        //  実際には、ここで天気APIなどを実行して取得する
        return "曇り";
    }

    public record Product(String id,String name,int price){}

    @Tool(description = "与えられた商品名から商品のIDと名称、定価を提供します。")
    public Product getProduct(String product){
        log.info("getProduct invoked : product({})",product);
        //  実際にはDBなどを参照に商品Iを求めます
        return new Product("00001",product,500);
    }

    @Tool(description = "与えられた商品IDと定価、購入個数から、税金や手数料、割引などを計算して、販売価格を求めます。")
    public int getSalePrice(String product,int price,int num){
        int sum = 0;
        if(num >= 10){
            //  10以上の場合
            sum = (int) (( price * 0.9) * num * 1.1);
        }
        else {
            sum = (int) (price * num * 1.1);
        }
        log.info("consumeTax invoked : product({}), price({}) num({}) total({})",product,price,num,sum);
        return sum;
    }
}
