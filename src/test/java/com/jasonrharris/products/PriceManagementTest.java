package com.jasonrharris.products;

import com.jasonrharris.repositories.PriceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class PriceManagementTest {
    @Mock
    private PriceRepository priceRepository;

    private PriceManagement priceManagement;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        priceManagement = new PriceManagement(priceRepository);
    }

    @Test
    public void shouldAddPriceToMatchedProductWithNoPriceTest() {
        Product originalProduct = new Product(0L, "Test Prod", null);

        Price newPrice = Price.createPrice(originalProduct, "20.20", "GBP");
        Product updatedProduct = new Product(0L, originalProduct.getName(), newPrice);

        when(priceRepository.save(newPrice)).thenReturn(newPrice);

        Assert.assertEquals(newPrice, priceManagement.getExistingOrSaveUpdatedPrice(updatedProduct, originalProduct));
    }

    @Test
    public void shouldAddPriceToMatchedProductTest() {
        Product originalProduct = new Product(0L, "Test Prod", null);
        Price originalPrice = Price.createPrice(originalProduct, "20.20", "GBP");
        originalProduct = new Product(0L, originalProduct.getName(), originalPrice);

        Price newPrice = Price.createPrice(originalProduct, "20.40", "GBP");
        Product updatedProduct = new Product(0L, originalProduct.getName(), newPrice);

        when(priceRepository.save(newPrice)).thenReturn(newPrice);

        Assert.assertEquals(newPrice, priceManagement.getExistingOrSaveUpdatedPrice(updatedProduct, originalProduct));
    }

    @Test
    public void shouldNotSaveNullPrice() {
        priceManagement.saveNewProductPrice(new Product(1L, "Test Product", null));
        verify(priceRepository, never()).save(any(Price.class));
    }

    @Test
    public void shouldSaveNonNullPrice() {

        Product originalProduct = new Product(0L, "Test Prod", null);
        Price newPrice = Price.createPrice(originalProduct, "20.20", "GBP");
        originalProduct = new Product(0L, originalProduct.getName(), newPrice);

        when(priceRepository.save(newPrice)).thenReturn(newPrice);
        Assert.assertEquals(newPrice, priceManagement.saveNewProductPrice(originalProduct).orElseThrow());
    }
}
