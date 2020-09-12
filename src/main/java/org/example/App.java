package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

/**
 * Hello world!
 *
 */
public class App 
{
    static Pattern pattern = Pattern.compile("(\\d{1,2}) (\\d{1,2}(?:,\\d{1,2})?)");

    public static void main( String[] args ) throws InterruptedException, IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in));) {
            while (true) {
                System.out.println( "请输入增加投票次数, 投票id用英文逗号分隔，例如：5 1,2" );
                String input = br.readLine();
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    int n = Integer.parseInt(matcher.group(1));
                    String voteIds = matcher.group(2);
                    System.out.println("解析到投票次数："+n+"投票id: "+voteIds);
                    Vote vote = new Vote();
                    vote.vote(n,voteIds);
                    break;
                }else {
                    System.err.println("请输入正确参数");
                }
            }
        }
    }

}
