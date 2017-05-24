package com.globi;



import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class to implement transactional integration tests using the root application configuration.
 * 
 * @author Girish Lakshmanan
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@Transactional
@SpringBootTest
public abstract class AbstractIntegrationTest extends XMLTestCase {}
