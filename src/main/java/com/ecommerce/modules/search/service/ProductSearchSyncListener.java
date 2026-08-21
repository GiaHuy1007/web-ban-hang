package com.ecommerce.modules.search.service;

import com.ecommerce.common.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSearchSyncListener {

    private final ProductSearchService searchService;

    @Async
    @EventListener
    public void onProductUpdated(ProductUpdatedEvent event) {
        log.info("Received ProductUpdatedEvent: productId={}", event.getProductId());
        searchService.syncProductToElasticsearch(event.getProductId());
    }
}
