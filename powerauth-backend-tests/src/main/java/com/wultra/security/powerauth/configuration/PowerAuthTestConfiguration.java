/*
 * PowerAuth test and related software components
 * Copyright (C) 2018 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.security.powerauth.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import com.wultra.security.powerauth.test.PowerAuthTestSetUp;
import com.wultra.security.powerauth.test.PowerAuthTestTearDown;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import io.getlime.security.powerauth.lib.cmd.util.RestClientConfiguration;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.security.PublicKey;
import java.security.Security;
import java.util.UUID;

/**
 * Configuration for the PowerAuth test.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
public class PowerAuthTestConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PowerAuthTestConfiguration.class);

    @Value("${powerauth.rest.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.soap.url}")
    private String powerAuthServiceUrl;

    @Value("${powerauth.integration.service.url}")
    private String powerAuthIntegrationUrl;

    @Value("${powerauth.nextstep.service.url}")
    private String nextStepServiceUrl;

    @Value("${powerauth.custom.service.url}")
    private String customServiceUrl;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.test.application.name}")
    private String applicationName;

    @Value("${powerauth.test.application.version}")
    private String applicationVersion;

    @Value("${powerauth.test.client.transport}")
    private String clientTransport;

    private String applicationVersionForTests;
    private String applicationKey;
    private String applicationSecret;

    private Long applicationId;
    private Long versionId;
    private PublicKey masterPublicKeyConverted;

    private PowerAuthTestSetUp setUp;
    private PowerAuthTestTearDown tearDown;

    private KeyConvertor keyConvertor = new KeyConvertor();
    private ObjectMapper objectMapper = RestClientConfiguration.defaultMapper();

    // Version 3.1 temporary storage
    private File statusFileV31;
    private JSONObject resultStatusObjectV31 = new JSONObject();
    private String activationIdV31;
    private String userV31;

    // Version 3.0 temporary storage
    private File statusFileV3;
    private JSONObject resultStatusObjectV3 = new JSONObject();
    private String activationIdV3;
    private String userV3;

    // Version 2.1 temporary storage
    private File statusFileV2;
    private JSONObject resultStatusObjectV2 = new JSONObject();
    private String activationIdV2;
    private String userV2;

    private String password = "1234";

    @Autowired
    public void setPowerAuthTestSetUp(PowerAuthTestSetUp setUp) {
        this.setUp = setUp;
    }

    @Autowired
    public void setPowerAuthTestTearDown(PowerAuthTestTearDown tearDown) {
        this.tearDown = tearDown;
    }

    /**
     * Initialize PowerAuth client.
     * @return PowerAuth client.
     */
    @Bean
    public PowerAuthClient powerAuthClient() {
        try {
            return new PowerAuthRestClient(powerAuthRestUrl);
        } catch (PowerAuthClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Bean
    public NextStepClient nextStepClient() {
        try {
            return new NextStepClient(nextStepServiceUrl);
        } catch (NextStepClientException ex) {
            return null;
        }
    }

    @Bean
    public PowerAuthTestSetUp testSetUp() {
        return new PowerAuthTestSetUp();
    }

    @Bean
    public PowerAuthTestTearDown testTearDown() {
        return new PowerAuthTestTearDown();
    }

    @PostConstruct
    public void setUp() throws Exception {
        // Add Bouncy Castle Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Prepare common userId
        final String userId = UUID.randomUUID().toString();

        // Create status file and user for version 3.1
        statusFileV31 = File.createTempFile("pa_status_v31", ".json");
        userV31 = "TestUserV31_" + userId;

        // Create status file and user for version 3.0
        statusFileV3 = File.createTempFile("pa_status_v3", ".json");
        userV3 = "TestUserV3_" + userId;

        // Create status file and user for version 2.1
        statusFileV2 = File.createTempFile("pa_status_v2", ".json");
        userV2 = "TestUserV2_" + userId;

        // Random application name
        applicationVersionForTests = applicationVersion + "_" + System.currentTimeMillis();

        setUp.execute();
    }

    @PreDestroy
    public void tearDown() throws PowerAuthClientException {
        tearDown.execute();
    }

    public String getPowerAuthServiceUrl() {
        return powerAuthServiceUrl;
    }

    public String getPowerAuthIntegrationUrl() {
        return powerAuthIntegrationUrl;
    }

    public String getCustomServiceUrl() {
        return customServiceUrl;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersionForTests;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getApplicationVersionId() {
        return versionId;
    }

    public void setApplicationVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public PublicKey getMasterPublicKey() {
        return masterPublicKeyConverted;
    }

    public KeyConvertor getKeyConvertor() {
        return keyConvertor;
    }

    public File getStatusFileV31() {
        return statusFileV31;
    }

    public JSONObject getResultStatusObjectV31() {
        return resultStatusObjectV31;
    }

    public File getStatusFileV3() {
        return statusFileV3;
    }

    public JSONObject getResultStatusObjectV3() {
        return resultStatusObjectV3;
    }

    public File getStatusFileV2() {
        return statusFileV2;
    }

    public JSONObject getResultStatusObjectV2() {
        return resultStatusObjectV2;
    }

    public String getActivationIdV31() {
        return activationIdV31;
    }

    public void setActivationIdV31(String activationIdV31) {
        this.activationIdV31 = activationIdV31;
    }

    public String getActivationIdV3() {
        return activationIdV3;
    }

    public void setActivationIdV3(String activationIdV3) {
        this.activationIdV3 = activationIdV3;
    }

    public String getActivationIdV2() {
        return activationIdV2;
    }

    public void setActivationIdV2(String activationIdV2) {
        this.activationIdV2 = activationIdV2;
    }

    public String getPassword() {
        return password;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public String getUserV2() {
        return userV2;
    }

    public String getUserV3() {
        return userV3;
    }

    public String getUserV31() {
        return userV31;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public void setMasterPublicKey(String masterPublicKey) {
        // Convert master public key
        byte[] masterKeyBytes = BaseEncoding.base64().decode(masterPublicKey);
        try {
            masterPublicKeyConverted = keyConvertor.convertBytesToPublicKey(masterKeyBytes);
        } catch (Exception ex) {
        }
    }
}