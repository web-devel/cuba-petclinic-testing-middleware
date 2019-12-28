package com.haulmont.sample.petclinic;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.testsupport.TestContainer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class PetclinicTestContainer extends TestContainer {

    public PetclinicTestContainer() {
        super();
        appComponents = new ArrayList<>(Arrays.asList(
                "com.haulmont.cuba"
                // add CUBA premium add-ons here
                // "com.haulmont.bpm",
                // "com.haulmont.charts",
                // "com.haulmont.fts",
                // "com.haulmont.reports",
                // and custom app components if any
        ));
        appPropertiesFiles = Arrays.asList(
                // List the files defined in your web.xml
                // in appPropertiesConfig context parameter of the core module
            "com/haulmont/sample/petclinic/app.properties",
                // Add this file which is located in CUBA and defines some properties
                // specifically for test environment. You can replace it with your own
                // or add another one in the end.
                "com/haulmont/cuba/testsupport/test-app.properties");
        initDbProperties();
    }

    private void initDbProperties() {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("com/company/cejt/app.properties");
        if (resource.exists()) {
            try (InputStream inputStream = Files.newInputStream(resource.getFile().toPath())) {
                Properties properties = new Properties();
                properties.load(inputStream);
                if ((Objects.equals(properties.getProperty("cuba.dataSourceProvider"), "application"))) {
                    Optional.ofNullable(properties.getProperty("driverClassName")).ifPresent(e -> dbDriver = e);
                    Optional.ofNullable(properties.getProperty("cuba.dataSource.jdbcUrl")).ifPresent(e -> dbUrl = e);
                    Optional.ofNullable(properties.getProperty("cuba.dataSource.username")).ifPresent(e -> dbUser = e);
                    Optional.ofNullable(properties.getProperty("cuba.dataSource.password")).ifPresent(e -> dbPassword = e);
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException("Cannot find 'app.properties' file to read database connection properties. " +
                        "You can set them explicitly in this method.");
            }
        }

        File contextXmlFile = new File("modules/core/web/META-INF/context.xml");
        if (!contextXmlFile.exists()) {
            contextXmlFile = new File("web/META-INF/context.xml");
        }

        if (!contextXmlFile.exists()) {
            throw new RuntimeException("Cannot find 'context.xml' file to read database connection properties. " +
                    "You can set them explicitly in this method.");
        }
        Document contextXmlDoc = Dom4j.readDocument(contextXmlFile);
        Element resourceElem = contextXmlDoc.getRootElement().element("Resource");

        Optional.ofNullable(resourceElem.attributeValue("driverClassName")).ifPresent(e -> dbDriver = e);
        Optional.ofNullable(resourceElem.attributeValue("url")).ifPresent(e -> dbUrl = e);
        Optional.ofNullable(resourceElem.attributeValue("username")).ifPresent(e -> dbUser = e);
        Optional.ofNullable(resourceElem.attributeValue("password")).ifPresent(e -> dbPassword = e);
    }

    public static class Common extends PetclinicTestContainer {

        public static final PetclinicTestContainer.Common INSTANCE = new PetclinicTestContainer.Common();

        private static volatile boolean initialized;

        private Common() {
        }

        @Override
        public void before() throws Throwable {
            if (!initialized) {
                super.before();
                initialized = true;
            }
            setupContext();
        }

        @Override
        public void after() {
            cleanupContext();
            // never stops - do not call super
        }
    }
}