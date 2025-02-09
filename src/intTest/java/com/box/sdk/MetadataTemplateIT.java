package com.box.sdk;

import static com.box.sdk.BoxApiProvider.jwtApiForServiceAccount;
import static com.box.sdk.CleanupTools.deleteFile;
import static com.box.sdk.CleanupTools.deleteFolder;
import static com.box.sdk.MetadataQuery.OrderBy.ascending;
import static com.box.sdk.UniqueTestFolder.getUniqueFolder;
import static com.box.sdk.UniqueTestFolder.removeUniqueFolder;
import static com.box.sdk.UniqueTestFolder.setupUniqeFolder;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link MetadataTemplate} related integration tests.
 */
public class MetadataTemplateIT {
    @BeforeClass
    public static void setup() {
        setupUniqeFolder();
    }

    @AfterClass
    public static void tearDown() {
        removeUniqueFolder();
    }

    @Test
    public void testDeleteMetadataTemplateSucceeds() {
        String scope = "enterprise";
        String template = "testtemplate";
        String displayName = "Test Template";
        int errorResponseStatusCode = 404;

        BoxAPIConnection api = jwtApiForServiceAccount();

        try {
            MetadataTemplate.createMetadataTemplate(api, scope, template, displayName, false, null);
        } catch (BoxAPIException e) {
            System.out.print("Error while making callout to createMetdataTemplate(): " + e);
        }

        MetadataTemplate.deleteMetadataTemplate(api, scope, template);

        try {
            MetadataTemplate.getMetadataTemplate(api, template);
        } catch (BoxAPIException e) {
            assertEquals(errorResponseStatusCode, e.getResponseCode());
        }
    }

    @Test
    public void createMetadataTemplateSucceeds() {
        BoxAPIConnection api = jwtApiForServiceAccount();

        MetadataTemplate.Field ctField = new MetadataTemplate.Field();
        ctField.setType("string");
        ctField.setKey("customerTeam");
        ctField.setDisplayName("Customer Team");

        MetadataTemplate.Field fyField = new MetadataTemplate.Field();
        fyField.setType("enum");
        fyField.setKey("fy");
        fyField.setDisplayName("FY");

        List<String> options = new ArrayList<>();
        options.add("FY16");
        options.add("FY17");
        fyField.setOptions(options);

        List<MetadataTemplate.Field> fields = new ArrayList<>();
        fields.add(ctField);
        fields.add(fyField);

        try {
            MetadataTemplate.createMetadataTemplate(api, "enterprise",
                "documentFlow03", "Document Flow 03", false, fields);
        } catch (BoxAPIException apiEx) {
            //Delete MetadataTemplate is yet to be supported. Due to that template might be existing already.
            //This expects the conflict error. To check the MetadataTemplate creation, please replace the id.
            assertEquals(409, apiEx.getResponseCode());
        }

        MetadataTemplate storedTemplate = MetadataTemplate.getMetadataTemplate(api, "documentFlow03");
        Assert.assertNotNull(storedTemplate);
    }

    private List<MetadataTemplate.FieldOperation> addFieldsHelper() {
        List<MetadataTemplate.FieldOperation> fieldOperations = new ArrayList<>();
        MetadataTemplate.FieldOperation customerFieldOp = new MetadataTemplate.FieldOperation();
        customerFieldOp.setOp(MetadataTemplate.Operation.addField);

        MetadataTemplate.Field customerTeam = new MetadataTemplate.Field();
        customerTeam.setType("string");
        customerTeam.setKey("customerTeam");
        customerTeam.setDisplayName("Customer Team");
        customerFieldOp.setData(customerTeam);
        fieldOperations.add(customerFieldOp);

        MetadataTemplate.FieldOperation departmentFieldOp = new MetadataTemplate.FieldOperation();
        departmentFieldOp.setOp(MetadataTemplate.Operation.addField);

        MetadataTemplate.Field deptField = new MetadataTemplate.Field();
        deptField.setType("enum");
        deptField.setKey("department");
        deptField.setDisplayName("Department");

        List<String> options = new ArrayList<>();
        options.add("Beauty");
        options.add("Shoes");
        deptField.setOptions(options);
        departmentFieldOp.setData(deptField);

        fieldOperations.add(departmentFieldOp);
        return fieldOperations;
    }

    @Test
    public void updateMetadataTemplateFieldsSucceeds() {
        BoxAPIConnection api = jwtApiForServiceAccount();

        try {
            //Test adding fields
            List<MetadataTemplate.FieldOperation> fieldOperations = this.addFieldsHelper();
            MetadataTemplate template = MetadataTemplate.updateMetadataTemplate(api,
                "enterprise", "documentFlow03", fieldOperations);
            Assert.assertNotNull(template);

            boolean foundDeptField = false;
            boolean foundCustField = false;
            for (MetadataTemplate.Field field : template.getFields()) {
                if ("department".equals(field.getKey())) {
                    assertEquals("enum", field.getType());
                    assertEquals("Department", field.getDisplayName());
                    assertEquals(2, field.getOptions().size());
                    foundDeptField = true;
                } else if ("customerTeam".equals(field.getKey())) {
                    foundCustField = true;
                }
            }
            assertTrue("department field was not found", foundDeptField);
            assertTrue("customer field was not found", foundCustField);

            //Test editing fields
            fieldOperations.clear();

            MetadataTemplate.FieldOperation customerFieldOp = new MetadataTemplate.FieldOperation();
            customerFieldOp.setOp(MetadataTemplate.Operation.editField);
            customerFieldOp.setFieldKey("customerTeam");

            MetadataTemplate.Field customerTeam = new MetadataTemplate.Field();
            customerTeam.setDisplayName("Customer Team Renamed");
            customerTeam.setKey("newCustomerTeamKey");
            customerFieldOp.setData(customerTeam);
            fieldOperations.add(customerFieldOp);

            MetadataTemplate.FieldOperation editEnumOption = new MetadataTemplate.FieldOperation();
            editEnumOption.setOp(MetadataTemplate.Operation.editEnumOption);
            editEnumOption.setFieldKey("department");
            editEnumOption.setEnumOptionKey("Shoes");


            MetadataTemplate.Field deptField = new MetadataTemplate.Field();
            deptField.setKey("Baby");
            editEnumOption.setData(deptField);

            fieldOperations.add(editEnumOption);

            template = MetadataTemplate.updateMetadataTemplate(api,
                "enterprise", "documentFlow03", fieldOperations);
            boolean foundBabyEnumOption = false;
            for (MetadataTemplate.Field field : template.getFields()) {
                if ("customerTeam".equals(field.getKey())) {
                    Assert.fail("'customerTeam' field key should have been changed to 'newCustomerTeamKey'");
                } else if ("department".equals(field.getKey())) {
                    for (String option : field.getOptions()) {
                        if ("Baby".equals(option)) {
                            foundBabyEnumOption = true;
                            break;
                        }
                    }
                } else if ("newCustomerTeamKey".equals(field.getKey())) {
                    assertEquals("Display name should have been updated",
                        "Customer Team Renamed", field.getDisplayName());
                }
            }
            assertTrue("Baby enum option was not found", foundBabyEnumOption);

            //Test removing fields
            fieldOperations.clear();

            MetadataTemplate.FieldOperation deleteDeptField = new MetadataTemplate.FieldOperation();
            deleteDeptField.setOp(MetadataTemplate.Operation.removeField);
            deleteDeptField.setFieldKey("newCustomerTeamKey");
            fieldOperations.add(deleteDeptField);

            MetadataTemplate.FieldOperation deleteEnumOption = new MetadataTemplate.FieldOperation();
            deleteEnumOption.setOp(MetadataTemplate.Operation.removeEnumOption);
            deleteEnumOption.setFieldKey("department");
            deleteEnumOption.setEnumOptionKey("Baby");

            fieldOperations.add(deleteEnumOption);

            template = MetadataTemplate.updateMetadataTemplate(api, "enterprise", "documentFlow03", fieldOperations);

            for (MetadataTemplate.Field field : template.getFields()) {
                if ("newCustomerTeamKey".equals(field.getKey())) {
                    Assert.fail("newCustomerTeamKey field key should have been deleted");
                } else if ("department".equals(field.getKey())) {
                    for (String option : field.getOptions()) {
                        if ("Baby".equals(option)) {
                            Assert.fail("Baby enum option should have been deleted");
                        }
                    }
                }
            }
        } finally {
            this.tearDownFields(api);
        }
    }

    @Test
    public void getAllMetadataSucceeds() {
        BoxFile uploadedFile = null;
        BoxAPIConnection api = jwtApiForServiceAccount();
        try {
            BoxFolder rootFolder = BoxFolder.getRootFolder(api);
            String fileName = "[getAllMetadataSucceeds] Test File.txt";
            byte[] fileBytes = "Non-empty string".getBytes(StandardCharsets.UTF_8);

            InputStream uploadStream = new ByteArrayInputStream(fileBytes);
            uploadedFile = rootFolder.uploadFile(uploadStream, fileName).getResource();

            uploadedFile.createMetadata(new Metadata().add("/firstName", "John").add("/lastName", "Smith"));
            Metadata check1 = uploadedFile.getMetadata();
            Assert.assertNotNull(check1);
            assertEquals("John", check1.getString("/firstName"));
            assertEquals("Smith", check1.getString("/lastName"));

            //Create fields before test
            List<MetadataTemplate.FieldOperation> fieldOperations = this.addFieldsHelper();
            MetadataTemplate template = MetadataTemplate.updateMetadataTemplate(api,
                "enterprise", "documentFlow03", fieldOperations);
            Assert.assertNotNull(template);

            Metadata customerMetaData = new Metadata();
            customerMetaData.add("/customerTeam", "MyTeam");
            customerMetaData.add("/department", "Beauty");

            uploadedFile.createMetadata("documentFlow03", "enterprise", customerMetaData);

            Iterable<Metadata> allMetadata = uploadedFile.getAllMetadata("/firstName", "/lastName");
            Assert.assertNotNull(allMetadata);
            Iterator<Metadata> iter = allMetadata.iterator();
            int numTemplates = 0;
            while (iter.hasNext()) {
                Metadata metadata = iter.next();
                numTemplates++;
                if (metadata.getTemplateName().equals("properties")) {
                    assertEquals("John", metadata.getString("/firstName"));
                    assertEquals("Smith", metadata.getString("/lastName"));
                }
                if (metadata.getTemplateName().equals("documentFlow03")) {
                    assertEquals("MyTeam", metadata.getString("/customerTeam"));
                    assertEquals("Beauty", metadata.getString("/department"));
                }
            }
            Assert.assertTrue("Should have at least 2 templates", numTemplates >= 2);
        } finally {
            deleteFile(uploadedFile);
            this.tearDownFields(api);
        }
    }

    @Test
    public void executeMetadataTemplateQuery() {
        BoxAPIConnection api = jwtApiForServiceAccount();
        String templateKey = "MyTemplate";
        BoxFolder one = null;
        BoxFolder two = null;

        try {
            MetadataTemplate.Field metadataField = new MetadataTemplate.Field();
            metadataField.setType("float");
            metadataField.setKey("myField");
            metadataField.setDisplayName("Value");
            List<MetadataTemplate.Field> fields = new ArrayList<>();
            fields.add(metadataField);
            MetadataTemplate.createMetadataTemplate(api, "enterprise", templateKey, "My Template", false, fields);

            BoxFolder rootFolder = getUniqueFolder(api);
            one = rootFolder.createFolder("one").getResource();
            one.createMetadata(templateKey, "enterprise", new Metadata().add("/myField", 102d));
            two = rootFolder.createFolder("two").getResource();
            two.createMetadata(templateKey, "enterprise", new Metadata().add("/myField", 95d));


            MetadataQuery query = new MetadataQuery(format("enterprise_%s.MyTemplate", TestConfig.getEnterpriseID()))
                .setQuery("myField > :val")
                .addParameter("val", 100)
                .setOrderBy(ascending("myField"));
            BoxResourceIterable<BoxItem.Info> result = MetadataTemplate.executeMetadataQuery(api, query);
            Iterator<BoxItem.Info> iterator = result.iterator();
            BoxItem.Info foundFolder = iterator.next();
            assertThat(foundFolder.getName(), is("one"));
            assertThat(iterator.hasNext(), is(false));
        } finally {
            MetadataTemplate.deleteMetadataTemplate(api, "enterprise", templateKey);
            deleteFolder(one);
            deleteFolder(two);
        }
    }

    private void tearDownFields(BoxAPIConnection api) {
        List<MetadataTemplate.FieldOperation> fieldOperations = new ArrayList<>();
        MetadataTemplate template = MetadataTemplate.getMetadataTemplate(api, "documentFlow03", "enterprise");
        for (MetadataTemplate.Field field : template.getFields()) {
            MetadataTemplate.FieldOperation deleteField = new MetadataTemplate.FieldOperation();
            deleteField.setOp(MetadataTemplate.Operation.removeField);
            deleteField.setFieldKey(field.getKey());
            fieldOperations.add(deleteField);
        }
        MetadataTemplate.updateMetadataTemplate(api, "enterprise", "documentFlow03", fieldOperations);
    }

}
