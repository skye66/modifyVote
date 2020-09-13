package org.example;

import cn.hutool.core.net.URLEncoder;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Vote {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String headerStr = "{\n" +
            "    \"accept\": \"*/*\",\n" +
            "    \"accept-language\": \"zh-CN,zh;q=0.9,en-CN;q=0.8,en;q=0.7\",\n" +
            "    \"content-type\": \"application/x-www-form-urlencoded; charset=UTF-8\",\n" +
            "    \"sec-fetch-dest\": \"empty\",\n" +
            "    \"sec-fetch-mode\": \"cors\",\n" +
            "    \"sec-fetch-site\": \"cross-site\"\n" +
            "  }";
    static Pattern pattern = Pattern.compile("voteId\":\"(.*)\"");

    public void test() throws InterruptedException {
        vote(10,"");
    }

    public void vote(int n,String voteIds) throws InterruptedException {
        // 查询当前票数
//        queryCurrentVote();
        logger.info("增加投票次数：{}", n);
        while (n-- > 0) {
            // 获取投标随机码
            String va_random = getRandomCode();
            if (Objects.isNull(va_random)) return;
            int i = new Random().nextInt(10000) + 5000;
            logger.info("拿到随机码，休息一下：{}s", i / 1000);
            Thread.sleep(i);
            // 发起投票
            doVote(va_random,voteIds);
//            int i1 = new Random().nextInt(20000) + 6000;
//            logger.info("投票成功，休息一下：{}s", i1 / 1000);
            logger.info("投票成功");
//            Thread.sleep(i1);
        }
        // 更新后票数
        queryCurrentVote(voteIds);

    }

    private String getRandomCode() {
        String url = String.format("https://h5.ebdan.net/ls/NoO0kXYE?share_level=1&from_user=20200911%s&from_id=%s&share_time=%s",
                UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID().toString().substring(0, 10),
                System.currentTimeMillis() + ""
        );
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();

        Matcher matcher = pattern.matcher(body);
        String va_random = "";
        if (matcher.find()) {
            va_random = matcher.group(1);
            logger.info("获取到随机投票码:{}", va_random);
        } else {
            logger.info("获取随机投票码失败 ....");
            return null;
        }
        return va_random;
    }

    private void doVote(String va_random, String voteIds) {
        URLEncoder aDefault = URLEncoder.createQuery();
        String encodeId = aDefault.encode(voteIds, Charset.defaultCharset());
        String url = "https://form-preview-api.eqxiu.com/lp/r/10022681/171583793?code=NoO0kXYE";
        String e = "5piT5LyB56eALeihqOWNleS6p+WTgTpodHRwczovL3d3dy5lcXhpdS5jb20vZWlwL3NjZW5lP3R5cGU9bGY=10022681";
        String va_code = SecureUtil.md5(va_random + e);
        Map<String, String> map = new JSONObject(headerStr).entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> (String) entry.getValue()));
        MultiValueMap<String, String> header = new HttpHeaders();
        header.setAll(map);
//        String body = "eq%5Bf_vote_3705252189%5D=6%2C30&costTime=22793&fromDetail=&eq%5Bf_va_random%5D=" + va_random + "&eq%5Bf_va_code%5D=" + va_code;
        String body = "eq%5Bf_vote_3705252189%5D=" +
                encodeId + "&costTime=22793&fromDetail=&eq%5Bf_va_random%5D=" +
                va_random + "&eq%5Bf_va_code%5D=" +
                va_code;
        HttpEntity httpEntity = new HttpEntity(body, header);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> objectResponseEntity = restTemplate.postForEntity(url, httpEntity, Object.class);
        logger.info("投票返回结果：{}", objectResponseEntity);
    }

    private void queryCurrentVote(String voteIds) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> forEntity = restTemplate.getForEntity("https://form-preview-api.eqxiu.com/lp/vote/data/all?pageId=10022681", JSONObject.class);
        JSONObject body = forEntity.getBody();
        Map<String, Integer> rankMap = ((JSONObject) ((JSONObject) body.get("obj")).get("3705252189")).toBean(Map.class);
        List<String> idList = rankMap.entrySet().stream().map(entry -> entry.getKey()).collect(Collectors.toList());
        idList.sort((id2, id1) -> rankMap.get(id1).compareTo(rankMap.get(id2)));

        String[] split = voteIds.split(",");
        for (String s : split) {
            logger.info("投票id:{},当前排名：{},当前票数：{}",s,idList.indexOf(s)+1, rankMap.get(s));
        }
        idList.forEach(k-> {
            logger.info("投票id:{},当前排名：{},当前票数：{}",k,idList.indexOf(k)+1, rankMap.get(k));
        });
    }

}
