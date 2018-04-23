package platform.work.pluto.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JdExport {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    public static String path = "/Users/swy/Documents/jjdd/";
    @Resource
    private JdbcTemplate jdbcTemplate;

    //@Scheduled(cron = "*/6 * * * * ?")
    @Scheduled(fixedRate = 1000 * 60 * 60 * 24 * 5)
    private void process() {
        final LobHandler lobHandler = new DefaultLobHandler();
        List<Map<String, Object>> query = new ArrayList<Map<String, Object>>();
        LOGGER.info("京东人脸导入开始！");
        //批量取数据
        int step = 2;
        for (int m = 0; m <= 9000; m = m + step) {
            LOGGER.info("执行第{}-{}条开始", m, m + step);
            query = jdbcTemplate.query("select id,answer from tags_task_libs  where tid=465 limit " + m + "," + step, new RowMapper() {
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                    //以二进制的数组方式获得Blob数据,第二个参数3是指blob字段在结果集的位置
                    byte[] attach = lobHandler.getBlobAsBytes(rs, 2);
                    //非blob子弹获取
                    String key = rs.getString("id");
                    String strAttach = new String(attach);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("id", key);
                    map.put("answer", strAttach);
                    return map;
                }
            });
            if (query == null || query.size() == 0) {
                LOGGER.info("第{}-{}条无数据", m, m + step);
                continue;
            }
            System.out.println("data size:" + query.size());

            //批次逐条处理
            for (int k = 0; k < query.size(); k++) {
                if (StringUtils.isEmpty(query.get(k).get("answer"))) {
                    continue;
                }
                //解析json数组
                try {
                    //System.out.println(new String(toByteArray(query.get(k).get("answer"))));
                    JSONObject answer = JSON.parseObject(query.get(k).get("answer").toString());
                    JSONArray result = answer.getJSONObject("markData").getJSONArray("marks");
                    int ep = 0;
                    for (int j = 0; j < result.size(); j++) {
                        try {
                            JSONObject job = result.getJSONObject(j);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                            if (job.getString("visible").contains("2") && ep < 5) {
                                LOGGER.info("用户：{}地址：{}", job.getString("rrId"), job.getString("url"));
                                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path, "/" + job.getString("rrId") + ".txt"), true));
                                writer.append(job.getString("url") + "\r\n");
                                writer.flush();
                                writer.close();
                                ep++;
                            }
                        } catch (Exception ex) {
                            LOGGER.info("ex:{}", ex.getMessage());
                        }
                    }

                } catch (Exception ex1) {
                    LOGGER.info("ex1:{}", ex1.getMessage());
                }
            }
            LOGGER.info("执行第{}-{}条完成", m, m + step);
        }
        LOGGER.info("京东人脸导出结束！");

    }


    public byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }
}
