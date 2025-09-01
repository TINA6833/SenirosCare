package com.ryanshiun.seniorscare.bus.fare;

public class FareCalculator {

	// 桃園計程車資：起程1,250m 95元；續程每250m 5元（不含延滯）
    public static int calcTaxiFareByTYMeters(int meters) {
    	if (meters <= 0) 
    	    return 95;
    	
    	if (meters <= 1250)
    		return 95;
    	
    	int extra = meters - 1250;
    	int steps = (int) Math.ceil(extra/250.0);
    	return 95 + steps*5;
    }

    // 復康巴士＝計程車資的三分之一，無條件進位 
    public static int calcRehabusFareFormTaxi(int taxiFare) {
    	return (int) Math.ceil(taxiFare/3.0);
    	//ceil 函數將十進制數轉換為直接最大整數
    }
}
