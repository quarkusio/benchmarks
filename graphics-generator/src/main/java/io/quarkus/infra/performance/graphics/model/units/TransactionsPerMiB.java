package io.quarkus.infra.performance.graphics.model.units;

public class TransactionsPerMiB extends DimensionalNumber {

    public TransactionsPerMiB(double throughput) {
        super(throughput);
    }

    @Override
    public String getUnits() {
        return "transactions/MiB";
    }
}
