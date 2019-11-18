package com.jasonrharris;

import com.fasterxml.classmate.TypeResolver;
import com.jasonrharris.controllers.OrderController;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private final TypeResolver resolver;

    public SwaggerConfig(@Autowired TypeResolver resolver) {
        this.resolver = resolver;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .additionalModels(resolver.resolve(OrderController.OrderBodyType.class))
                .select()
                .apis(RequestHandlerSelectors.any())
                .build();
    }
}
