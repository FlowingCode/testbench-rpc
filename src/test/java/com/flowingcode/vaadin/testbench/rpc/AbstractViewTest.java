/*-
 * #%L
 * RPC for Vaadin TestBench
 * %%
 * Copyright (C) 2021 - 2023 Flowing Code
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
package com.flowingcode.vaadin.testbench.rpc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.parallel.ParallelTest;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Base class for ITs. The tests use Chrome driver to run integration tests on a headless Chrome.
 */
public abstract class AbstractViewTest extends ParallelTest {

  private static final int SERVER_PORT = 8080;

  private final String route;

  protected static final Matcher<TestBenchElement> hasBeenUpgradedToCustomElement =
      new TypeSafeDiagnosingMatcher<TestBenchElement>() {

        @Override
        public void describeTo(Description description) {
          description.appendText("a custom element");
        }

        @Override
        protected boolean matchesSafely(TestBenchElement item, Description mismatchDescription) {
          String script = "let s=arguments[0].shadowRoot; return !!(s&&s.childElementCount)";
          if (!item.getTagName().contains("-")) {
            return true;
          }

          if ((Boolean) item.getCommandExecutor().executeScript(script, item)) {
            return true;
          } else {
            mismatchDescription.appendText(item.getTagName() + " ");
            mismatchDescription.appendDescriptionOf(is(not(this)));
            return false;
          }
        }
      };

  @Rule public ScreenshotOnFailureRule rule = new ScreenshotOnFailureRule(this, true);

  protected AbstractViewTest(String route) {
    this.route = route;
  }

  @BeforeClass
  public static void setupClass() {
    WebDriverManager.chromedriver().setup();
  }

  @Override
  @Before
  public void setup() throws Exception {
    setDriver(TestBench.createDriver(new ChromeDriver()));
    getDriver().get(getURL(route));
  }

  @After
  public void after() {
    getDriver().close();
  }

  /**
   * Returns deployment host name concatenated with route.
   *
   * @return URL to route
   */
  protected static String getURL(String route) {
    return String.format("http://%s:%d/%s", getDeploymentHostname(), SERVER_PORT, route);
  }

  /**
   * If running on CI, get the host name from environment variable HOSTNAME
   *
   * @return the host name
   */
  private static String getDeploymentHostname() {
    return "localhost";
  }
}
