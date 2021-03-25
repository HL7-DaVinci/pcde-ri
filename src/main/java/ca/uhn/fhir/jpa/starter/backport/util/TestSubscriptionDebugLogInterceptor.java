package ca.uhn.fhir.jpa.starter.backport.util;

/*-
 * #%L
 * HAPI FHIR Subscription Server
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.searchparam.matcher.InMemoryMatchResult;
import ca.uhn.fhir.jpa.subscription.model.CanonicalSubscriptionChannelType;
import ca.uhn.fhir.jpa.subscription.util.SubscriptionDebugLogInterceptor;
import ca.uhn.fhir.jpa.subscription.model.ResourceDeliveryMessage;
import ca.uhn.fhir.jpa.subscription.model.ResourceModifiedMessage;
import ca.uhn.fhir.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Date;
import java.util.EnumMap;
import java.util.function.Function;

/**
 * This interceptor can be used for troubleshooting subscription processing. It provides very
 * detailed logging about the subscription processing pipeline.
 * <p>
 * This interceptor loges each step in the processing pipeline with a
 * different event code, using the event codes itemized in
 * {@link EventCodeEnum}. By default these are each placed in a logger with
 * a different name (e.g. <code>ca.uhn.fhir.jpa.subscription.util.SubscriptionDebugLogInterceptor.SUBS20</code>
 * in order to facilitate fine-grained logging controls where some codes are omitted and
 * some are not.
 * </p>
 * <p>
 * A custom log factory can also be passed in, in which case the logging
 * creation may use another strategy.
 * </p>
 *
 * @see EventCodeEnum
 * @since 3.7.0
 */
public class TestSubscriptionDebugLogInterceptor extends SubscriptionDebugLogInterceptor {

}
