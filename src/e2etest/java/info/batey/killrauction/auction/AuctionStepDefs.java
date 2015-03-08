package info.batey.killrauction.auction;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import info.batey.killrauction.client.AuctionServiceClient;
import info.batey.killrauction.client.GetAuctionResponse;
import org.apache.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AuctionStepDefs {
    @Given("^the auction does not already exist$")
    public void the_auction_does_not_already_exist() throws Throwable {
    }

    @When("^a user creates an auction$")
    public void a_user_creates_an_auction() throws Throwable {
        HttpResponse response = AuctionServiceClient.instance.createAuction("new_auction");
        assertEquals(201, response.getStatusLine().getStatusCode());
    }

    @Then("^other users can see the auction$")
    public void other_users_can_see_the_auction() throws Throwable {
        GetAuctionResponse newAuction = AuctionServiceClient.instance.getAuction("new_auction");
        assertNotNull(newAuction);
        assertEquals("new_auction", newAuction.getName());
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
        // Express the Regexp above with the code you wish you had
        throw new PendingException();
    }

    @And("^the bid is viewable by others$")
    public void the_bid_is_viewable_by_others() throws Throwable {
        // Express the Regexp above with the code you wish you had
        throw new PendingException();
    }
}