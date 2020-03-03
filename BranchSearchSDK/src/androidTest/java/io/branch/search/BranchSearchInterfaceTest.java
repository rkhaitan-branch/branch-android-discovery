package io.branch.search;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.branch.search.util.AssetUtils;

/**
 * BranchSearchInterface class tests.
 */
@RunWith(AndroidJUnit4.class)
public class BranchSearchInterfaceTest extends BranchTest {

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        initBranch();
        // Spy all the network handlers.
        URLConnectionNetworkHandler[] handlers = BranchSearch.getInstance().networkHandlers;
        for (int i = 0; i < handlers.length; i++) {
            handlers[i] = Mockito.spy(handlers[i]);
        }
        // Spy the raw handler.
        BranchSearchInterface.sRawHandler = Mockito.spy(BranchSearchInterface.sRawHandler);
    }

    @Test
    public void testSearch_successful() throws Throwable {
        // When search is executed, return the success_mex_food.json JSON.
        URLConnectionNetworkHandler searchHandler
                = BranchSearch.getInstance().getNetworkHandler(BranchSearch.Channel.SEARCH);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                IURLConnectionEvents events = invocation.getArgument(2);
                String response = AssetUtils.readJsonFile(getTestContext(), "success_mex_food.json");
                JSONObject object = new JSONObject(response);
                events.onResult(object);
                return null;
            }
        }).when(searchHandler).executePost(
                Mockito.anyString(),
                Mockito.any(JSONObject.class),
                Mockito.any(IURLConnectionEvents.class));
        BranchSearchRequest request = BranchSearchRequest.Create("food");

        // Perform the request and ensure we have results.
        final CountDownLatch latch = new CountDownLatch(1);
        BranchSearchInterface.search(request, new IBranchSearchEvents() {
            @Override
            public void onBranchSearchResult(BranchSearchResult result) {
                Assert.assertNotNull(result);
                Assert.assertTrue(result.getResults().size() > 0);
                latch.countDown();
            }

            @Override
            public void onBranchSearchError(BranchSearchError error) {
                throw new RuntimeException("Should not happen.");
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testSearch_triggersServiceEnabled() throws Throwable {
        // Prepare the search response. We want it to return UNAUTHORIZED_ERR so that
        // the service enabled should be triggered immediately.
        URLConnectionNetworkHandler searchHandler
                = BranchSearch.getInstance().getNetworkHandler(BranchSearch.Channel.SEARCH);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                IURLConnectionEvents events = invocation.getArgument(2);
                events.onResult(new BranchSearchError(BranchSearchError.ERR_CODE.UNAUTHORIZED_ERR));
                return null;
            }
        }).when(searchHandler).executePost(
                Mockito.anyString(),
                Mockito.any(JSONObject.class),
                Mockito.any(IURLConnectionEvents.class));
        BranchSearchRequest request = BranchSearchRequest.Create("pizza");

        // When service enabled is triggered...
        // Case 1: return ENABLED. So the final error should be UNAUTHORIZED_ERR.
        URLConnectionNetworkHandler serviceEnabledHandler = BranchSearchInterface.sRawHandler;
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                IURLConnectionEvents events = invocation.getArgument(1);
                events.onResult(new JSONObject());
                return null;
            }
        }).when(serviceEnabledHandler).executeGet(
                Mockito.anyString(),
                Mockito.any(IURLConnectionEvents.class)
        );
        doSearchAndWaitForError(request, BranchSearchError.ERR_CODE.UNAUTHORIZED_ERR);

        // When service enabled is triggered...
        // Case 2: return DISABLED. So the final error should be SERVICE_DISABLED_ERR.
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                IURLConnectionEvents events = invocation.getArgument(1);
                JSONObject result = new JSONObject();
                result.put("disabled", true);
                events.onResult(result);
                return null;
            }
        }).when(serviceEnabledHandler).executeGet(
                Mockito.anyString(),
                Mockito.any(IURLConnectionEvents.class)
        );
        doSearchAndWaitForError(request, BranchSearchError.ERR_CODE.SERVICE_DISABLED_ERR);
    }

    private void doSearchAndWaitForError(
            @NonNull BranchSearchRequest request,
            @NonNull final BranchSearchError.ERR_CODE expected) throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        BranchSearchInterface.search(request, new IBranchSearchEvents() {
            @Override
            public void onBranchSearchResult(BranchSearchResult result) {
                throw new RuntimeException("Should not happen.");
            }

            @Override
            public void onBranchSearchError(BranchSearchError error) {
                Assert.assertEquals(expected, error.getErrorCode());
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}
