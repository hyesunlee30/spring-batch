package com.bdlee.spring.batch;


import com.bdlee.spring.batch.job.QuerydslPagingItemReaderConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestBatchConfig.class, QuerydslPagingItemReaderConfiguration.class})
public class BatchApplicationTests {


    @Test
    public void READER_TEST() throws Exception {

    }


    @Test
    public void EXECUTION_JOB_TEST() throws Exception {


    }

}
