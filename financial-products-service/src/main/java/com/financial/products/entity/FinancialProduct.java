package com.financial.products.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("financial_products")
public class FinancialProduct {

    @Id
    private Long id;

    @Column("codigo_unico")
    private String codigoUnico;

    @Column("tipo_producto")
    private TipoProducto tipoProducto;

    @Column("nombre")
    private String nombre;

    @Column("saldo")
    private BigDecimal saldo;

    @Column("numero_cuenta")
    private String numeroCuenta;

    @Column("estado")
    private EstadoProducto estado;

    @Column("fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum para tipos de productos financieros
     */
    public enum TipoProducto {
        CUENTA_AHORRO("Cuenta de Ahorro"),
        CUENTA_CORRIENTE("Cuenta Corriente"),
        TARJETA_CREDITO("Tarjeta de Crédito"),
        TARJETA_DEBITO("Tarjeta de Débito"),
        PRESTAMO("Préstamo"),
        DEPOSITO_PLAZO_FIJO("Depósito a Plazo Fijo"),
        CREDITO_HIPOTECARIO("Crédito Hipotecario");

        private final String descripcion;

        TipoProducto(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    /**
     * Enum para estados de productos
     */
    public enum EstadoProducto {
        ACTIVO("Activo"),
        INACTIVO("Inactivo"),
        SUSPENDIDO("Suspendido"),
        CERRADO("Cerrado");

        private final String descripcion;

        EstadoProducto(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}