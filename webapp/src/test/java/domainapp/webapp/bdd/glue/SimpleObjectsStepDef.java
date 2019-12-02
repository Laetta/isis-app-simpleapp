/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package domainapp.webapp.bdd.glue;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import domainapp.webapp.application.fixture.scenarios.DomainAppDemo;
import domainapp.webapp.integtests.ApplicationIntegTestAbstract;
import domainapp.modules.simple.dom.impl.SimpleObject;
import domainapp.modules.simple.dom.impl.SimpleObjects;
import domainapp.modules.simple.fixture.SimpleObject_persona;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.val;

public class SimpleObjectsStepDef extends ApplicationIntegTestAbstract {

    @Given("^there are.* (\\d+) simple objects$")
    public void there_are_N_simple_objects(int n) throws Throwable {
        final List<SimpleObject> list = wrap(simpleObjects).listAll();
        assertThat(list.size(), is(n));
    }

    @When("^.*create a .*simple object$")
    public void create_a_simple_object() throws Throwable {
        wrap(simpleObjects).create(UUID.randomUUID().toString());
    }

    // -- TRANSACTION ASPECT

    private Runnable afterScenario;

    @Before //TODO is there another way to make scenarios transactional?
    public void beforeScenario(){
        val txTemplate = new TransactionTemplate(txMan);
        val status = txTemplate.getTransactionManager().getTransaction(null);
        afterScenario = () -> {
            txTemplate.getTransactionManager().rollback(status);
        };

		fixtureScripts.runPersonas(
		        SimpleObject_persona.BANG, 
		        SimpleObject_persona.BAR, 
		        SimpleObject_persona.BAZ);

        status.flush();
    } 

    @After
    public void afterScenario(){
        if(afterScenario==null) {
            return;
        }
        afterScenario.run();
        afterScenario = null;
    }


    // -- DEPENDENCIES

    @Inject protected SimpleObjects simpleObjects;
    @Inject private FixtureScripts fixtureScripts; 
    @Inject private PlatformTransactionManager txMan;

    @Before(value="@DomainAppDemo", order=20000)
    public void runDomainAppDemo() {
        fixtureScripts.runFixtureScript(new DomainAppDemo(), null); // <1>
    }

}