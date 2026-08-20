package com.demoproject.core.services.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.dam.cfm.FragmentTemplate;

import com.demoproject.core.services.ProductFragmentService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(service = ProductFragmentService.class)
public class ProductFragmentServiceImpl
        implements ProductFragmentService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProductFragmentServiceImpl.class
            );

    private static final String SUBSERVICE =
            "asset-rendition";

    private static final String JSON_FILE =
            "/products.json";

    private static final String MODEL_PATH =
            "/conf/demoproject/settings/dam/cfm/models/product";

    private static final String PARENT_PATH =
            "/content/dam/demoproject/products";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;


    @Override
    public String createProductFragments() {

        log.info("======================================");
        log.info("PRODUCT FRAGMENT SERVICE STARTED");
        log.info("======================================");

        List<String> createdFragments =
                new ArrayList<>();

        /*
         * STEP 1:
         * Load products.json from classpath.
         */
        try (InputStream jsonStream =
                     ProductFragmentServiceImpl.class
                             .getResourceAsStream(
                                     JSON_FILE
                             )) {

            if (jsonStream == null) {

                log.error(
                        "Could not find products.json in classpath: {}",
                        JSON_FILE
                );

                return createErrorResponse(
                        "products.json not found"
                );
            }

            log.info(
                    "products.json successfully loaded from classpath"
            );


            /*
             * STEP 2:
             * Read JSON file.
             */
            String jsonText =
                    new String(
                            jsonStream.readAllBytes(),
                            StandardCharsets.UTF_8
                    );


            /*
             * STEP 3:
             * Parse JSON using Gson.
             */
            JsonObject root =
                    JsonParser.parseString(
                            jsonText
                    ).getAsJsonObject();


            JsonObject data =
                    root.getAsJsonObject(
                            "data"
                    );


            if (data == null) {

                log.error(
                        "Missing 'data' object in products.json"
                );

                return createErrorResponse(
                        "Missing data object in JSON"
                );
            }


            JsonObject productList =
                    data.getAsJsonObject(
                            "productList"
                    );


            if (productList == null) {

                log.error(
                        "Missing 'productList' object in products.json"
                );

                return createErrorResponse(
                        "Missing productList object in JSON"
                );
            }


            JsonArray items =
                    productList.getAsJsonArray(
                            "items"
                    );


            if (items == null) {

                log.error(
                        "Missing 'items' array in products.json"
                );

                return createErrorResponse(
                        "Missing items array in JSON"
                );
            }


            log.info(
                    "Number of products found in JSON: {}",
                    items.size()
            );


            /*
             * STEP 4:
             * Obtain service resource resolver.
             */
            Map<String, Object> authInfo =
                    Collections.singletonMap(
                            ResourceResolverFactory.SUBSERVICE,
                            SUBSERVICE
                    );


            try (ResourceResolver resolver =
                         resourceResolverFactory
                                 .getServiceResourceResolver(
                                         authInfo
                                 )) {


                /*
                 * STEP 5:
                 * Get Product Content Fragment Model.
                 */
                Resource modelResource =
                        resolver.getResource(
                                MODEL_PATH
                        );


                if (modelResource == null) {

                    log.error(
                            "Product model not found: {}",
                            MODEL_PATH
                    );

                    return createErrorResponse(
                            "Product model not found"
                    );
                }


                log.info(
                        "Product model found: {}",
                        MODEL_PATH
                );


                /*
                 * Convert model resource into
                 * FragmentTemplate.
                 */
                FragmentTemplate fragmentTemplate =
                        modelResource.adaptTo(
                                FragmentTemplate.class
                        );


                if (fragmentTemplate == null) {

                    log.error(
                            "Could not adapt model to FragmentTemplate: {}",
                            MODEL_PATH
                    );

                    return createErrorResponse(
                            "Could not adapt Product model"
                    );
                }


                log.info(
                        "Product model successfully adapted to FragmentTemplate"
                );


                /*
                 * STEP 6:
                 * Get the DAM folder where
                 * Content Fragments will be created.
                 */
                Resource parentResource =
                        resolver.getResource(
                                PARENT_PATH
                        );


                if (parentResource == null) {

                    log.error(
                            "Product folder not found: {}",
                            PARENT_PATH
                    );

                    return createErrorResponse(
                            "Product folder not found"
                    );
                }


                log.info(
                        "Product parent folder found: {}",
                        PARENT_PATH
                );


                /*
                 * STEP 7:
                 * Loop through every product
                 * in the JSON array.
                 */
                for (int i = 0;
                     i < items.size();
                     i++) {

                    JsonElement item =
                            items.get(i);


                    if (!item.isJsonObject()) {

                        log.warn(
                                "Skipping invalid product at index: {}",
                                i
                        );

                        continue;
                    }


                    JsonObject product =
                            item.getAsJsonObject();


                    /*
                     * Read productName.
                     */
                    String productName =
                            getStringValue(
                                    product,
                                    "productName"
                            );


                    /*
                     * Read productId.
                     */
                    String productId =
                            getStringValue(
                                    product,
                                    "productId"
                            );


                    /*
                     * Read description.plaintext.
                     */
                    String description = "";


                    if (product.has("description")
                            && !product
                                    .get("description")
                                    .isJsonNull()) {

                        JsonObject descriptionObject =
                                product
                                        .getAsJsonObject(
                                                "description"
                                        );


                        if (descriptionObject.has(
                                "plaintext"
                        )) {

                            description =
                                    descriptionObject
                                            .get("plaintext")
                                            .getAsString();
                        }
                    }


                    /*
                     * Read price.
                     */
                    double price = 0;


                    if (product.has("price")
                            && !product
                                    .get("price")
                                    .isJsonNull()) {

                        price =
                                product
                                        .get("price")
                                        .getAsDouble();
                    }


                    /*
                     * Read category.
                     */
                    String category =
                            getStringValue(
                                    product,
                                    "category"
                            );


                    log.info(
                            "Processing product: name={}, id={}, price={}, category={}",
                            productName,
                            productId,
                            price,
                            category
                    );


                    /*
                     * STEP 8:
                     * Convert product name into
                     * repository-safe fragment name.
                     *
                     * Example:
                     *
                     * Lenovo ThinkPad
                     *        ↓
                     * lenovo-thinkpad
                     */
                    String fragmentName =
                            productName
                                    .toLowerCase()
                                    .replaceAll(
                                            "[^a-z0-9]+",
                                            "-"
                                    )
                                    .replaceAll(
                                            "(^-|-$)",
                                            ""
                                    );


                    /*
                     * Build fragment path.
                     */
                    String fragmentPath =
                            PARENT_PATH
                                    + "/"
                                    + fragmentName;


                    /*
                     * STEP 9:
                     * Check whether fragment already exists.
                     */
                    Resource existingResource =
                            resolver.getResource(
                                    fragmentPath
                            );


                    if (existingResource != null) {

                        log.info(
                                "Fragment already exists. Skipping: {}",
                                fragmentPath
                        );

                        continue;
                    }


                    /*
                     * STEP 10:
                     * Create Content Fragment
                     * using the AEM Product model.
                     */
                    ContentFragment fragment =
                            fragmentTemplate.createFragment(
                                    parentResource,
                                    fragmentName,
                                    productName
                            );


                    if (fragment == null) {

                        log.error(
                                "Could not create Content Fragment: {}",
                                productName
                        );

                        continue;
                    }


                    log.info(
                            "Content Fragment created: {}",
                            fragmentPath
                    );


                    /*
                     * STEP 11:
                     * Populate Product Name.
                     */
                    setStringValue(
                            fragment,
                            "productName",
                            productName
                    );


                    /*
                     * Populate Product ID.
                     */
                    setStringValue(
                            fragment,
                            "productId",
                            productId
                    );


                    /*
                     * Populate Description.
                     */
                    setStringValue(
                            fragment,
                            "description",
                            description
                    );


                    /*
                     * Populate Price.
                     */
                    setNumberValue(
                            fragment,
                            "price",
                            price
                    );


                    /*
                     * Populate Category.
                     */
                    setStringValue(
                            fragment,
                            "category",
                            category
                    );


                    /*
                     * STEP 12:
                     * Populate Product Image.
                     *
                     * JSON structure:
                     *
                     * "productImage": {
                     *     "_path": "/content/dam/..."
                     * }
                     */
                    if (product.has("productImage")
                            && !product
                                    .get("productImage")
                                    .isJsonNull()) {

                        JsonObject productImage =
                                product
                                        .getAsJsonObject(
                                                "productImage"
                                        );


                        if (productImage.has("_path")
                                && !productImage
                                        .get("_path")
                                        .isJsonNull()) {

                            String imagePath =
                                    productImage
                                            .get("_path")
                                            .getAsString();


                            setStringValue(
                                    fragment,
                                    "productImage",
                                    imagePath
                            );


                            log.info(
                                    "Product image set: {}",
                                    imagePath
                            );
                        }
                    }


                    /*
                     * Add successfully created
                     * fragment path to response.
                     */
                    createdFragments.add(
                            fragmentPath
                    );


                    log.info(
                            "Product fragment processing completed: {}",
                            fragmentPath
                    );
                }


                /*
                 * STEP 13:
                 * Persist repository changes.
                 */
                if (resolver.hasChanges()) {

                    resolver.commit();

                    log.info(
                            "All Content Fragment changes committed successfully"
                    );
                }
            }


            /*
             * STEP 14:
             * Return successful response.
             */
            log.info("======================================");
            log.info(
                    "PRODUCT FRAGMENT SERVICE COMPLETED"
            );
            log.info("======================================");


            return createResponse(
                    createdFragments
            );


        } catch (LoginException e) {

            log.error(
                    "Could not obtain service resource resolver",
                    e
            );

            return createErrorResponse(
                    "Could not obtain service resolver"
            );


        } catch (Exception e) {

            log.error(
                    "Error while creating product fragments",
                    e
            );

            return createErrorResponse(
                    e.getMessage()
            );
        }
    }


    /*
     * Helper method to safely read
     * a String property from JsonObject.
     */
    private String getStringValue(
            JsonObject object,
            String propertyName) {

        if (object == null
                || !object.has(propertyName)
                || object.get(propertyName).isJsonNull()) {

            return "";
        }

        return object
                .get(propertyName)
                .getAsString();
    }


    /*
     * Set String-based Content Fragment field.
     */
    private void setStringValue(
            ContentFragment fragment,
            String elementName,
            String value)
            throws Exception {

        ContentElement element =
                fragment.getElement(
                        elementName
                );


        if (element == null) {

            log.error(
                    "Element not found in Product model: {}",
                    elementName
            );

            return;
        }


        FragmentData data =
                element.getValue();


        data.setValue(value);


        element.setValue(data);


        log.info(
                "Field populated: {} = {}",
                elementName,
                value
        );
    }


    /*
     * Set Number-based Content Fragment field.
     */
    private void setNumberValue(
            ContentFragment fragment,
            String elementName,
            double value)
            throws Exception {

        ContentElement element =
                fragment.getElement(
                        elementName
                );


        if (element == null) {

            log.error(
                    "Number element not found in Product model: {}",
                    elementName
            );

            return;
        }


        FragmentData data =
                element.getValue();


        if (data.isTypeSupported(
                Double.class
        )) {

            data.setValue(value);

        } else if (data.isTypeSupported(
                Long.class
        )) {

            data.setValue(
                    (long) value
            );

        } else {

            throw new IllegalArgumentException(
                    "Price field does not support Double or Long"
            );
        }


        element.setValue(data);


        log.info(
                "Field populated: {} = {}",
                elementName,
                value
        );
    }


    /*
     * Create successful JSON response.
     */
    private String createResponse(
            List<String> createdFragments) {

        StringBuilder json =
                new StringBuilder();


        json.append("{");
        json.append("\"status\":\"success\",");
        json.append("\"createdFragments\":[");


        for (int i = 0;
             i < createdFragments.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }


            json.append("\"")
                    .append(
                            escapeJson(
                                    createdFragments.get(i)
                            )
                    )
                    .append("\"");
        }


        json.append("]");
        json.append("}");


        return json.toString();
    }


    /*
     * Create error JSON response.
     */
    private String createErrorResponse(
            String message) {

        return "{"
                + "\"status\":\"error\","
                + "\"message\":\""
                + escapeJson(message)
                + "\""
                + "}";
    }


    /*
     * Escape characters for JSON response.
     */
    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }


        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\r",
                        "\\r"
                );
    }
}