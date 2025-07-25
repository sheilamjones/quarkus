package io.quarkus.devui.testrunner.brokenonly;

import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.devui.tests.DevUIJsonRPCTest;
import io.quarkus.test.ContinuousTestingTestUtils;
import io.quarkus.test.ContinuousTestingTestUtils.TestStatus;
import io.quarkus.test.QuarkusDevModeTest;

public class TestBrokenOnlyTestCase extends DevUIJsonRPCTest {

    @RegisterExtension
    static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    return ShrinkWrap.create(JavaArchive.class).addClass(BrokenOnlyResource.class)
                            .add(new StringAsset(ContinuousTestingTestUtils.appProperties()),
                                    "application.properties");
                }
            })
            .setTestArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    return ShrinkWrap.create(JavaArchive.class).addClass(SimpleET.class);
                }
            });

    public TestBrokenOnlyTestCase() {
        super("devui-continuous-testing");
    }

    @Test
    public void testBrokenOnlyMode() throws InterruptedException, Exception {
        ContinuousTestingTestUtils utils = new ContinuousTestingTestUtils();
        TestStatus ts = utils.waitForNextCompletion();

        Assertions.assertEquals(1L, ts.getTestsFailed());
        Assertions.assertEquals(1L, ts.getTestsPassed());
        Assertions.assertEquals(0L, ts.getTestsSkipped());

        //start broken only mode
        super.executeJsonRPCMethod("toggleBrokenOnly");

        test.modifyTestSourceFile(SimpleET.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.replace("@QuarkusTest", "@QuarkusTest //noop change");
            }
        });
        ts = utils.waitForNextCompletion();

        Assertions.assertEquals(1L, ts.getTestsFailed());
        Assertions.assertEquals(0L, ts.getTestsPassed()); //passing test should not have been run
        Assertions.assertEquals(0L, ts.getTestsSkipped());

        test.modifySourceFile(BrokenOnlyResource.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.replace("//setup(router);", "setup(router);");
            }
        });
        ts = utils.waitForNextCompletion();

        Assertions.assertEquals(0L, ts.getTestsFailed());
        Assertions.assertEquals(1L, ts.getTestsPassed());
        Assertions.assertEquals(0L, ts.getTestsSkipped());

        //now add a new failing test
        test.modifyTestSourceFile(SimpleET.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.replace("//failannotation", "@Test");
            }
        });
        ts = utils.waitForNextCompletion();

        Assertions.assertEquals(1L, ts.getTestsFailed());
        Assertions.assertEquals(0L, ts.getTestsPassed());
        Assertions.assertEquals(0L, ts.getTestsSkipped());

        //now make it pass
        test.modifyTestSourceFile(SimpleET.class, new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.replace("Assertions.fail();", "//noop");
            }
        });
        ts = utils.waitForNextCompletion();
        Assertions.assertEquals(0L, ts.getTestsFailed());
        Assertions.assertEquals(1L, ts.getTestsPassed());
        Assertions.assertEquals(0L, ts.getTestsSkipped());

    }
}
