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
package com.wultra.security.powerauth.webflow.test;

import com.wultra.security.powerauth.webflow.configuration.WebFlowTestConfiguration;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static junit.framework.TestCase.assertTrue;

/**
 * Global test teardown.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PowerAuthTestTearDown {

    private PowerAuthServiceClient powerAuthClient;
    private WebFlowTestConfiguration config;

    @Autowired
    public void setPowerAuthServiceClient(PowerAuthServiceClient powerAuthClient) {
        this.powerAuthClient = powerAuthClient;
    }

    @Autowired
    public void setWebFlowTestConfiguration(WebFlowTestConfiguration config) {
        this.config = config;
    }

    public void execute() {
        powerAuthClient.removeActivation(config.getActivationId(), "test");
        assertTrue(config.getStatusFile().delete());
        try {
            config.getWebDriver().close();
            config.getWebDriver().quit();
        } catch (Exception ex) {
            // Ignore exceptions
        }
    }
}
