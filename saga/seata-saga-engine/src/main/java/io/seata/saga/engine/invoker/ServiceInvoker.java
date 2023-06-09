/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.saga.engine.invoker;

import io.seata.saga.statelang.domain.ServiceTaskState;

/**
 * Service invoker
 *
 * @author lorne.cl
 */
public interface ServiceInvoker {

    /**
     * invoke service
     * @param serviceTaskState the service task state
     * @param input optional params
     * @return the invoker result
     * @throws Throwable the throwable
     */
    Object invoke(ServiceTaskState serviceTaskState, Object... input) throws Throwable;
}