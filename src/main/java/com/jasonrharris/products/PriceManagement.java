package com.jasonrharris.products;

import com.jasonrharris.repositories.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

/**
 * PriceManagement is currently fairly rudimentary and will need fleshing out (e.g. PriceBooks, rules about how many prices a Product can have per Ccy etc.)
 */

@Service
public class PriceManagement {
    private final PriceRepository priceRepository;

    public PriceManagement(@Autowired PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public Price getExistingOrSaveUpdatedPrice(Product updatedProduct, Product matchingProduct) {

        Optional<Price> optUpdatedPrice = updatedProduct.getCurrentPrice();

        Price latestMatchingPrice = priceRepository.findTopByProductOrderByCreationDateTime(matchingProduct);

        if (optUpdatedPrice.isEmpty() || isUpdatedPriceUnchanged(optUpdatedPrice.get(), latestMatchingPrice)) {
            return latestMatchingPrice;
        } else {
            return savePrice(Price.createPrice(matchingProduct, optUpdatedPrice.get().getAmount(), optUpdatedPrice.get().getCurrency()));
        }
    }

    public Optional<Price> saveNewProductPrice(Product product) {
        return product.getCurrentPrice().map(price -> savePrice(Price.createPrice(product, price.getAmount(), price.getCurrency())));
    }

    private boolean isUpdatedPriceUnchanged(Price updatedPrice, Price latestMatchingPrice) {
        return latestMatchingPrice == updatedPrice || latestMatchingPrice.compareTo(updatedPrice) == 0;
    }

    private Price savePrice(Price price) {
        return priceRepository.save(price);
    }
}
