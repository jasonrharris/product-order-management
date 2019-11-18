package com.jasonrharris.products;

import com.jasonrharris.repositories.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Optional;

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

        if (optUpdatedPrice.isEmpty() || isPriceUnchanged(optUpdatedPrice.get(), matchingProduct)) {
            return matchingProduct.getCurrentPrice().orElse(null);
        } else {
            return savePrice(Price.createPrice(matchingProduct, optUpdatedPrice.get().getAmount(), optUpdatedPrice.get().getCurrency()));
        }
    }

    public Optional<Price> saveNewProductPrice(Product product) {
        return product.getCurrentPrice().map(price -> savePrice(Price.createPrice(product, price.getAmount(), price.getCurrency())));
    }

    private boolean isPriceUnchanged(@NotNull Price updatedPrice, Product matchingProduct) {
        if (updatedPrice.getAmount().equals(Price.UNSET_AMOUNT)) {
            return true;
        }
        if (matchingProduct.getCurrentPrice().isPresent()) {
            return updatedPrice.getAmount().equals(Price.UNSET_AMOUNT) || matchingProduct.getCurrentPrice().get().equals(updatedPrice) || matchingProduct.getCurrentPrice().get().compareTo(updatedPrice) == 0;
        }
        return false;
    }

    private Price savePrice(Price price) {
        return priceRepository.save(price);
    }
}
