package com.example.data

object FallbackData {

    val nseIndustries = listOf(
        IndustryPerformance("Nifty Auto", 3.4, 8.2, 18.5, 12, 3, "Super Leader", 88.5),
        IndustryPerformance("Nifty Financial", 1.8, 4.9, 12.1, 16, 4, "Steady Leader", 78.2),
        IndustryPerformance("Nifty IT", -0.5, 2.1, 6.8, 8, 7, "Consolidating / Neutral", 56.4),
        IndustryPerformance("Nifty Metal", 4.2, 9.1, 22.4, 14, 1, "Extreme Power Leader", 94.0),
        IndustryPerformance("Nifty Pharma", 0.2, 3.5, 9.2, 11, 4, "Improving / Constructive", 68.1),
        IndustryPerformance("Nifty FMCG", -1.2, -2.4, -1.5, 5, 10, "Laggard / Weakness", 32.5),
        IndustryPerformance("Nifty Energy", 2.1, 5.8, 14.5, 9, 3, "Steady Leader", 82.0),
        IndustryPerformance("Nifty Realty", 5.6, 12.4, 31.8, 8, 2, "Extreme Power Leader", 96.5)
    )

    val nseLiquidLeaders = listOf(
        LiquidLeader("TATAELXSI", "Tata Elxsi Ltd.", 7850.00, 4.5, "1.2M", 1, 92, 1.2),
        LiquidLeader("TATASTEEL", "Tata Steel Ltd.", 172.50, 3.8, "18.5M", 2, 89, 0.5),
        LiquidLeader("M&M", "Mahindra & Mahindra Ltd.", 2840.00, 2.9, "2.1M", 3, 87, 2.1),
        LiquidLeader("RELIANCE", "Reliance Industries Ltd.", 2945.00, 1.5, "4.8M", 4, 85, 3.4),
        LiquidLeader("BHARTIARTL", "Bharti Airtel Ltd.", 1420.00, 1.2, "3.5M", 5, 83, 4.0),
        LiquidLeader("INFY", "Infosys Ltd.", 1580.00, -0.8, "2.9M", 6, 75, 8.5),
        LiquidLeader("TCS", "Tata Consultancy Services Ltd.", 3850.00, -0.3, "1.8M", 7, 72, 6.2),
        LiquidLeader("HDFCBANK", "HDFC Bank Ltd.", 1530.00, 0.4, "9.5M", 8, 68, 12.4),
        LiquidLeader("ICICIBANK", "ICICI Bank Ltd.", 1115.00, 1.1, "8.2M", 9, 81, 2.5),
        LiquidLeader("LT", "Larsen & Toubro Ltd.", 3480.00, 2.2, "1.4M", 10, 84, 1.8)
    )

    val nseMomentumTopNames = listOf(
        MomentumTopName("COALINDIA", "Coal India Ltd.", 482.00, 5.2, "8.9M", 12.4, 28.5, "Pivot break-out!", 94.2),
        MomentumTopName("HAL", "Hindustan Aeronautics Ltd.", 4220.00, 4.1, "1.5M", 15.2, 34.1, "Super Momentum", 93.8),
        MomentumTopName("ADANIPORTS", "Adani Ports & SEZ Ltd.", 1410.00, 3.2, "3.2M", 9.8, 22.1, "Near Pivot Box", 88.5),
        MomentumTopName("BEL", "Bharat Electronics Ltd.", 285.00, 2.8, "11.2M", 11.1, 25.4, "Consolidation Range", 89.2),
        MomentumTopName("SBIN", "State Bank of India", 812.00, 1.6, "6.1M", 5.5, 14.2, "Moving Avg Crossover", 82.1),
        MomentumTopName("NTPC", "NTPC Ltd.", 365.00, 3.5, "12.4M", 8.4, 19.8, "Breakout above 20 DMA", 86.4),
        MomentumTopName("ONGC", "Oil & Natural Gas Corp.", 272.00, 4.8, "15.1M", 10.2, 24.5, "Volume Spike Breakout", 91.0),
        MomentumTopName("TATACOMM", "Tata Communications Ltd.", 1890.00, 2.5, "1.1M", 7.2, 16.8, "Pivot Box Tightening", 84.5)
    )

    val nseDoublers = listOf(
        DoublerItem("IREDA", "Indian Renewable Energy Agency Ltd.", 185.00, 9.8, "24.5M", 32.00, 215.00, 135.0, 34.2, "Mid Cap", 45.0),
        DoublerItem("SJVN", "SJVN Ltd.", 132.00, 6.2, "18.2M", 38.00, 170.00, 85.0, 28.5, "Mid Cap", 32.0),
        DoublerItem("HUDCO", "Housing & Urban Development Corp.", 242.00, 5.5, "12.6M", 54.00, 310.00, 90.0, 21.4, "Mid Cap", 28.0),
        DoublerItem("NBCC", "NBCC (India) Ltd.", 145.00, 4.8, "9.8M", 40.00, 195.00, 110.0, 42.1, "Small Cap", 38.0),
        DoublerItem("RVNL", "Rail Vikas Nigam Ltd.", 385.00, 3.1, "7.4M", 110.00, 425.00, 75.0, 38.2, "Mid Cap", 30.0),
        DoublerItem("IRFC", "Indian Railway Finance Corp.", 175.00, 2.5, "14.2M", 31.00, 192.00, 80.0, 25.1, "Large Cap", 24.0),
        DoublerItem("NHPC", "NHPC Ltd.", 98.40, 4.2, "22.0M", 41.50, 115.80, 65.0, 19.8, "Mid Cap", 20.0),
        DoublerItem("JANGI-M", "Jangi Hydro Power Corp.", 42.50, 8.4, "5.1M", 12.00, 52.00, 180.0, 14.5, "Micro Cap", 72.0)
    )

    val nasdaqIndustries = listOf(
        IndustryPerformance("Semiconductors (SOXX)", 4.2, 11.5, 24.8, 22, 4, "Extreme Power Leader", 96.2),
        IndustryPerformance("Information Tech (XLK)", 2.8, 6.9, 16.2, 45, 12, "Super Leader", 89.0),
        IndustryPerformance("Biotechnology (IBB)", 1.1, 3.8, 7.4, 30, 18, "Improving / Constructive", 64.5),
        IndustryPerformance("Internet & Retail (AMZN)", 0.5, 4.2, 9.1, 18, 14, "Steady Leader", 72.8),
        IndustryPerformance("Telecommunications", -0.2, -1.5, 2.1, 10, 15, "Laggard / Weakness", 41.0),
        IndustryPerformance("Software (IGV)", 2.4, 5.2, 12.8, 32, 8, "Strong Leader", 81.4),
        IndustryPerformance("Clean Energy", -1.8, -4.5, -8.2, 8, 22, "Extreme Laggard", 15.0),
        IndustryPerformance("Fintech & Payments", 1.2, 3.1, 8.5, 14, 10, "Consolidating / Neutral", 58.0)
    )

    val nasdaqLiquidLeaders = listOf(
        LiquidLeader("NVDA", "NVIDIA Corp.", 1215.00, 5.4, "42.1M", 1, 98, 1.0),
        LiquidLeader("AVGO", "Broadcom Inc.", 1385.00, 3.6, "3.1M", 2, 94, 1.5),
        LiquidLeader("MSFT", "Microsoft Corp.", 418.50, 1.8, "22.5M", 3, 88, 2.4),
        LiquidLeader("AAPL", "Apple Inc.", 192.20, 1.2, "48.2M", 4, 84, 3.8),
        LiquidLeader("NFLX", "Netflix Inc.", 640.00, 0.8, "4.5M", 5, 82, 4.5),
        LiquidLeader("TSLA", "Tesla Inc.", 178.40, -1.5, "62.1M", 6, 62, 18.2),
        LiquidLeader("META", "Meta Platforms Inc.", 495.20, -0.4, "14.2M", 7, 85, 5.1),
        LiquidLeader("GOOGL", "Alphabet Inc. (A)", 172.40, 0.5, "19.8M", 8, 80, 4.1),
        LiquidLeader("AMD", "Advanced Micro Devices Inc.", 164.80, 2.1, "34.5M", 9, 78, 11.2),
        LiquidLeader("QCOM", "Qualcomm Inc.", 212.50, 2.8, "9.4M", 10, 86, 2.0)
    )

    val nasdaqMomentumTopNames = listOf(
        MomentumTopName("SMCI", "Super Micro Computer Inc.", 785.00, 7.2, "8.5M", 24.5, 58.2, "Pivot break-out!", 98.8),
        MomentumTopName("ANET", "Arista Networks Inc.", 312.00, 3.8, "2.1M", 12.4, 29.1, "Super Momentum", 92.4),
        MomentumTopName("MU", "Micron Technology Inc.", 124.50, 3.2, "14.2M", 11.8, 25.2, "Near Pivot Box Target", 90.5),
        MomentumTopName("AMZN", "Amazon.com Inc.", 185.00, 1.4, "28.5M", 6.2, 15.4, "Moving Avg Breakout", 84.1),
        MomentumTopName("COIN", "Coinbase Global Inc.", 242.80, 8.4, "12.1M", 18.5, 42.1, "Crypto Momentum Wave", 95.0),
        MomentumTopName("ARM", "ARM Holdings Plc", 132.50, 6.1, "16.8M", 14.1, 38.6, "Volume Spike Breakout", 93.1),
        MomentumTopName("MSTR", "MicroStrategy Inc.", 1610.00, 9.2, "2.4M", 28.4, 72.8, "Super Momentum Wave", 97.4),
        MomentumTopName("CRWD", "CrowdStrike Holdings Inc.", 342.00, 1.5, "3.8M", 5.2, 18.4, "Tight Range Breakout", 85.2)
    )

    val nasdaqDoublers = listOf(
        DoublerItem("PLTR", "Palantir Technologies Inc.", 24.50, 6.4, "18.5M", 7.20, 27.50, 120.0, 68.2, "Large Cap", 42.0),
        DoublerItem("ASTS", "AST Spacemobile Inc.", 9.80, 12.5, "11.2M", 1.95, 14.50, 160.0, 120.5, "Mid Cap", 85.0),
        DoublerItem("HOLO", "MicroCloud Hologram Inc.", 1.85, 22.4, "42.0M", 0.85, 18.20, 350.0, 95.0, "Small Cap", 120.0),
        DoublerItem("ALAB", "Astera Labs Inc.", 74.50, 5.8, "2.8M", 38.00, 95.00, 85.0, 72.4, "Mid Cap", 50.0),
        DoublerItem("SOUN", "SoundHound AI Inc.", 5.12, 4.9, "14.5M", 1.45, 10.25, 110.0, 48.0, "Small Cap", 65.0),
        DoublerItem("BBAI", "BigBear.ai Holdings", 1.92, 3.4, "8.9M", 1.10, 4.80, 95.0, 28.4, "Small Cap", 25.0),
        DoublerItem("NNE", "Nano Nuclear Energy Inc.", 14.50, 11.2, "5.6M", 3.50, 22.10, 250.0, 54.0, "Micro Cap", 95.0),
        DoublerItem("RGTI", "Rigetti Computing Inc.", 1.15, 8.5, "6.2M", 0.72, 2.85, 150.0, 31.0, "Micro Cap", 40.0)
    )

    val fallbackAlerts = listOf(
        AlertSignal("a1", System.currentTimeMillis() - 300000, "IREDA", "Volume Spike Alert!", "IREDA (NSE) traded 3x average volume with 9.8% price surge, breaking above its short-term console pivot.", AlertLevel.URGENT),
        AlertSignal("a2", System.currentTimeMillis() - 1200000, "SMCI", "Pivot Box Breakout", "SMCI (NASDAQ) crossed the major daily pivot point at $780 on higher-than-average volume.", AlertLevel.BREAKOUT),
        AlertSignal("a3", System.currentTimeMillis() - 3600000, "Nifty Metal", "Sector Momentum Wave", "Nifty Metal Index indicates heavy aggressive accumulator buying, leading NSE sector indexes by +4.2% weekly.", AlertLevel.MOMENTUM),
        AlertSignal("a4", System.currentTimeMillis() - 7200000, "ASTS", "Momentum Trend Continuity", "ASTS (NASDAQ) surges 12.5% today, approaching 52-week high with high RRG Relative Strength.", AlertLevel.MOMENTUM)
    )
}
