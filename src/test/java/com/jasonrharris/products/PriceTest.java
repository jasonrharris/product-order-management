package com.jasonrharris.products;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class PriceTest {

    private Product prod = new Product();

    @Test
    public void shouldCreatePrice() {
        createAndCheckPrice("20.20", "GBP", "20.20");
    }

    @Test
    public void shouldCreateCorrectlyRoundedPriceWhenInitialNumberOfDecimalPlacesIsTooMany() {
        createAndCheckPrice("20.20467", "GBP", "20.20");
        createAndCheckPrice("20.208", "GBP", "20.21");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreatePriceIfCcyIsUnknown() {
        createAndCheckPrice("20.20", "Broken", "20.20");
    }

    @Test
    public void shouldValidatePricesAsEqualIfIdsAreZeroAndAmountsAndCcysAreSame() {
        Price price1 = Price.createPrice(prod, "20.20", "GBP");
        Price price2 = Price.createPrice(prod, "20.20", "GBP");
        assertEquals(price1, price2);
    }

    @Test
    public void shouldValidatePricesAsEqualIfIdsAreZeroAndAmountssAreDifferent() {
        Price price1 = Price.createPrice(prod, "20.50", "GBP");
        Price price2 = Price.createPrice(prod, "20.20", "GBP");
        assertNotEquals(price1, price2);
    }

    @Test
    public void shouldComparePricesAsEqualIfAmountsAndCcysAreSame() {
        Price price1 = Price.createPrice(prod, "20.20", "GBP");
        Price price2 = Price.createPrice(prod, "20.20", "GBP");
        assertEquals(0, price1.compareTo(price2));
    }

    @Test
    public void shouldComparePricesAsDifferentIfAmountsAreDifferentAndCcysAreSame() {
        Price price1 = Price.createPrice(prod, "20.20", "GBP");
        Price price2 = Price.createPrice(prod, "20.25", "GBP");
        assertTrue(price1.compareTo(price2) < 0);
        assertTrue(price2.compareTo(price1) > 0);
    }

    @Test
    public void shouldComparePricesAsDifferentIfAmountsAreSameAndCcysAreSameButProdsAreDifferent() {
        Price price1 = Price.createPrice(prod, "20.20", "GBP");
        Price price2 = Price.createPrice(new Product("Test2"), "20.25", "GBP");
        assertTrue(price1.compareTo(price2) < 0);
        assertTrue(price2.compareTo(price1) > 0);
    }

    @Test
    public void shouldComparePricesAsDifferentEqualIfAmountsAreSameAndCcysAreDifferent() {
        Price price1 = Price.createPrice(prod, "20.20", "USD");
        Price price2 = Price.createPrice(prod, "20.20", "GBP");
        assertTrue(price1.compareTo(price2) > 0);
        assertTrue(price2.compareTo(price1) < 0);
    }

    private void createAndCheckPrice(String inputAmount, String ccy, String expectedAmount) {
        Price goodPrice = Price.createPrice(prod, inputAmount, ccy);
        Assert.assertEquals(goodPrice.getAmount(), new BigDecimal(expectedAmount));
        Assert.assertEquals(goodPrice.getCurrency().getCurrencyCode(), ccy);
    }
}
