package lk.gamage.backend.healthbridgebackend.analytics.reports.service;

import lk.gamage.backend.healthbridgebackend.analytics.reports.dto.request.GenerateAnalyticsReportRequest;
import lk.gamage.backend.healthbridgebackend.analytics.reports.dto.response.AnalyticsReportCapabilitiesResponse;
import lk.gamage.backend.healthbridgebackend.analytics.reports.dto.response.AnalyticsReportResponse;

public interface AnalyticsReportsService {
    AnalyticsReportResponse generatePreview(GenerateAnalyticsReportRequest request);

    AnalyticsReportCapabilitiesResponse getCapabilities();
}
