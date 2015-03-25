package info.batey.killrauction.auction;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import info.batey.killrauction.client.AuctionServiceClient;
import info.batey.killrauction.client.GetAuctionResponse;
import info.batey.killrauction.domain.BidVo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;

public class AuctionStepDefs {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionStepDefs.class);
    public static final byte[] EMPTY_PAYLOAD = new byte[0];
    public static final String DEFAULT_AUCTION = "new_auction";
    public static final String DEFAULT_USERNAME = "username";

    private Optional<List<GetAuctionResponse>> allAuctions;
    private BidStreamClient bidStreamClient;

    @Given("^the auction does not already exist$")
    public void the_auction_does_not_already_exist() throws Throwable {
    }

    @When("^a user creates an auction$")
    public void a_user_creates_an_auction() throws Throwable {
        HttpResponse response = AuctionServiceClient.instance.createAuction(DEFAULT_AUCTION);
        assertEquals(201, response.getStatusLine().getStatusCode());
    }

    @Then("^other users can see the auction$")
    public void other_users_can_see_the_auction() throws Throwable {
        GetAuctionResponse newAuction = AuctionServiceClient.instance.getAuction(DEFAULT_AUCTION).get();
        assertNotNull(newAuction);
        assertEquals(DEFAULT_AUCTION, newAuction.getName());
        assertEquals(DEFAULT_USERNAME, newAuction.getOwner());
    }

    @Given("^all requests are made with a valid user$")
    public void all_requests_are_made_with_a_valid_user() throws Throwable {
        AuctionServiceClient.instance.createUser("username", "password");
        AuctionServiceClient.instance.useUserNameAndPassword("username", "password");
    }

    @Given("^an auction exists for an ipad$")
    public void an_auction_exists_for_an_ipad() throws Throwable {
        AuctionServiceClient.instance.createAuction("ipad");
    }

    @When("^a user makes a bid$")
    public void a_user_makes_a_bid() throws Throwable {
        AuctionServiceClient.instance.placeBid("ipad", 100);
    }

    @Then("^the bid is accepted$")
    public void the_bid_is_accepted() throws Throwable {
        AuctionServiceClient.LastResponse lastResponse = AuctionServiceClient.instance.getLastResponse();
        assertThat(lastResponse.statusCode(), equalTo(HttpStatus.SC_CREATED));
    }

    @And("^the bid is viewable by others$")
    public void the_bid_is_viewable_by_others() throws Throwable {
        Optional<GetAuctionResponse> bids = AuctionServiceClient.instance.getAuction("ipad");
        assertTrue("No bids returned", bids.isPresent());
        assertThat(bids.get().getBids(), hasItems(new BidVo(DEFAULT_USERNAME, 100l, null)));
    }

    @Given("^multiple auctions exist$")
    public void multiple_auctions_exist() throws Throwable {
        AuctionServiceClient.instance.createAuction("one", 1);
        AuctionServiceClient.instance.createAuction("two", 1);
        AuctionServiceClient.instance.createAuction("three", 1);
    }

    @When("^a user requests all auctions$")
    public void a_user_requests_all_auctions() throws Throwable {
        allAuctions = AuctionServiceClient.instance.getAllAuctions();
    }

    @Then("^the auctions are returned$")
    public void the_auctions_are_returned() throws Throwable {
        assertThat("Expected auctions to be returned", allAuctions.isPresent(), equalTo(true));
        List<GetAuctionResponse> getAuctionResponses = allAuctions.get();
        assertThat(getAuctionResponses.size(), equalTo(3));
        assertThat(getAuctionResponses, hasItems(
                new GetAuctionResponse("three", 1, DEFAULT_USERNAME, Collections.emptyList()),
                new GetAuctionResponse("one", 1, DEFAULT_USERNAME, Collections.emptyList()),
                new GetAuctionResponse("two", 1, DEFAULT_USERNAME, Collections.emptyList())));
    }

    @When("^a bidstream is requested$")
    public void a_bidstream_is_requested() throws Throwable {
        bidStreamClient = new BidStreamClient(DEFAULT_AUCTION);

    }
    @And("^a new bid is made$")
    public void a_new_bid_is_made() throws Throwable {
        AuctionServiceClient.instance.placeBid(DEFAULT_AUCTION, 101);
        AuctionServiceClient.instance.placeBid(DEFAULT_AUCTION, 102);
        AuctionServiceClient.instance.placeBid(DEFAULT_AUCTION, 103);
    }

    @Then("^the bidstream contains the new bid$")
    public void the_bidstream_contains_the_new_bid() throws Throwable {
        List<BidVo> bids = bidStreamClient.blockForMessages(3, Duration.ofSeconds(5));
        LOGGER.debug("Received bids are {}", bids);
        assertThat(bids.size(), equalTo(3));
        assertThat(bids, hasItems(
                new BidVo(DEFAULT_USERNAME, 101l, null),
                new BidVo(DEFAULT_USERNAME, 102l, null),
                new BidVo(DEFAULT_USERNAME, 103l, null)
        ));
    }


}
