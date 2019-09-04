package org.isda.cdm.calculation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;

import java.math.BigDecimal;

import org.isda.cdm.AdjustableDate;
import org.isda.cdm.AdjustableOrRelativeDate;
import org.isda.cdm.BusinessCenters;
import org.isda.cdm.BusinessDayAdjustments;
import org.isda.cdm.BusinessDayConventionEnum;
import org.isda.cdm.CalculationPeriodDates;
import org.isda.cdm.CalculationPeriodFrequency;
import org.isda.cdm.ContractualQuantity;
import org.isda.cdm.DayCountFractionEnum;
import org.isda.cdm.FloatingRateIndexEnum;
import org.isda.cdm.FloatingRateSpecification;
import org.isda.cdm.InterestRatePayout;
import org.isda.cdm.NonNegativeAmountSchedule;
import org.isda.cdm.NotionalSchedule;
import org.isda.cdm.PeriodExtendedEnum;
import org.isda.cdm.RateSpecification;
import org.isda.cdm.RollConventionEnum;
import org.isda.cdm.functions.CalculationPeriod;
import org.isda.cdm.functions.CalculationPeriodImpl;
import org.isda.cdm.functions.GetRateSchedule;
import org.isda.cdm.functions.GetRateScheduleImpl;
import org.isda.cdm.functions.PeriodsInYear;
import org.isda.cdm.functions.ResolveRateIndex;
import org.isda.cdm.functions.ResolveRateIndexImpl;
import org.isda.cdm.functions.ToAdjustedDateFunctionImpl;
import org.isda.cdm.metafields.FieldWithMetaDayCountFractionEnum;
import org.isda.cdm.metafields.FieldWithMetaFloatingRateIndexEnum;
import org.isda.cdm.metafields.FieldWithMetaString;
import org.isda.cdm.metafields.ReferenceWithMetaBusinessCenters;
import org.junit.jupiter.api.Test;

import com.rosetta.model.lib.records.DateImpl;

class FloatingAmountTest {

    private static final InterestRatePayout INTEREST_RATE_PAYOUT = InterestRatePayout.builder()
            .setQuantity(ContractualQuantity.builder()
                    .setNotionalSchedule(NotionalSchedule.builder()
                            .setNotionalStepSchedule((NonNegativeAmountSchedule) NonNegativeAmountSchedule.builder()
                                    .setCurrency(FieldWithMetaString.builder().setValue("EUR").build())
                                    .setInitialValue(BigDecimal.valueOf(50_000_000))
                                    .build())
                            .build())
                    .build())
            .setRateSpecificationBuilder(RateSpecification.builder()
                    .setFloatingRateBuilder(FloatingRateSpecification.builder()
                            .setFloatingRateIndex(FieldWithMetaFloatingRateIndexEnum.builder()
                                    .setValue(FloatingRateIndexEnum.GBP_LIBOR_BBA)
                                    .build())))
            .setDayCountFraction(FieldWithMetaDayCountFractionEnum.builder().setValue(DayCountFractionEnum._30E_360).build())
            .setCalculationPeriodDates(CalculationPeriodDates.builder()
                    .setEffectiveDate((AdjustableOrRelativeDate.builder()
                            .setAdjustableDate(AdjustableDate.builder()
                                    .setUnadjustedDate(DateImpl.of(2018, 1, 3))
                                    .setDateAdjustments(BusinessDayAdjustments.builder()
                                            .setBusinessDayConvention(BusinessDayConventionEnum.NONE)
                                            .build())
                                    .build())
                            .build()))
                    .setTerminationDate(AdjustableOrRelativeDate.builder()
                            .setAdjustableDate(AdjustableDate.builder()
                                    .setUnadjustedDate(DateImpl.of(2020, 1, 3))
                                    .setDateAdjustments(BusinessDayAdjustments.builder()
                                            .setBusinessDayConvention(BusinessDayConventionEnum.MODFOLLOWING)
                                            .setBusinessCenters(BusinessCenters.builder()
                                                    .setBusinessCentersReference(ReferenceWithMetaBusinessCenters.builder()
                                                            .setExternalReference("primaryBusinessCenters")
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .setCalculationPeriodFrequency((CalculationPeriodFrequency) CalculationPeriodFrequency.builder()
                            .setRollConvention(RollConventionEnum._3)
                            .setPeriodMultiplier(3)
                            .setPeriod(PeriodExtendedEnum.M)
                            .build())
                    .setCalculationPeriodDatesAdjustments(BusinessDayAdjustments.builder()
                            .setBusinessDayConvention(BusinessDayConventionEnum.MODFOLLOWING)
                            .setBusinessCenters(BusinessCenters.builder()
                                    .setBusinessCentersReference(ReferenceWithMetaBusinessCenters.builder()
                                            .setExternalReference("primaryBusinessCenters")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();

    @Test
    void shouldApplyMultiplication() {
        ResolveRateIndex resolveRateIndex = new ResolveRateIndexImpl();  // 0.0875
        GetRateSchedule getRateSchedule = new GetRateScheduleImpl();
        CalculationPeriod calculationPeriod = new CalculationPeriodImpl(DateImpl.of(2018, 1, 3));
        PeriodsInYear periodsInYear = new PeriodsInYear() {

			@Override
			protected Integer doEvaluate(CalculationPeriodFrequency frequency) {
				return 4;
			}};
		ToAdjustedDateFunctionImpl toAdjustedDate = new ToAdjustedDateFunctionImpl();

        FloatingAmount.CalculationResult result = new FloatingAmount(resolveRateIndex, getRateSchedule, calculationPeriod, periodsInYear, toAdjustedDate)
                .calculate(INTEREST_RATE_PAYOUT);

        assertThat(result.getFloatingAmount(), closeTo(BigDecimal.valueOf(1093750), BigDecimal.valueOf(0.0000001)));
    }


}