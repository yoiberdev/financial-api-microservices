package com.financial.products.config;

import com.financial.products.entity.FinancialProduct;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(
                R2dbcCustomConversions.StoreConversions.NONE,
                List.of(
                        new TipoProductoWriteConverter(),
                        new TipoProductoReadConverter(),
                        new EstadoProductoWriteConverter(),
                        new EstadoProductoReadConverter()
                )
        );
    }

    // ============================
    // CONVERTERS PARA ENUMS
    // ============================

    @WritingConverter
    public static class TipoProductoWriteConverter implements Converter<FinancialProduct.TipoProducto, String> {
        @Override
        public String convert(FinancialProduct.TipoProducto source) {
            return source.name();
        }
    }

    @ReadingConverter
    public static class TipoProductoReadConverter implements Converter<String, FinancialProduct.TipoProducto> {
        @Override
        public FinancialProduct.TipoProducto convert(String source) {
            return FinancialProduct.TipoProducto.valueOf(source);
        }
    }

    @WritingConverter
    public static class EstadoProductoWriteConverter implements Converter<FinancialProduct.EstadoProducto, String> {
        @Override
        public String convert(FinancialProduct.EstadoProducto source) {
            return source.name();
        }
    }

    @ReadingConverter
    public static class EstadoProductoReadConverter implements Converter<String, FinancialProduct.EstadoProducto> {
        @Override
        public FinancialProduct.EstadoProducto convert(String source) {
            return FinancialProduct.EstadoProducto.valueOf(source);
        }
    }
}
