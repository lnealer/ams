package org.example.am.shared.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.dao.ModifyConfigurationDAO;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.service.ModifyConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compares the stored configuration revision against what the device reports and resolves the
 * mismatch either way.
 */
@Service("modifyConfigurationService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ModifyConfigurationServiceImpl implements ModifyConfigurationService {

    @Autowired
    private ModifyConfigurationDAO modifyConfigurationDAO;

    @Autowired
    private AssetConfigDAO assetConfigDAO;

    @Override
    public AssetConfiguration getDeviceReportedConfiguration(final long assetId) {
        return modifyConfigurationDAO.getDeviceReportedConfiguration(assetId);
    }

    @Override
    public List<String> compare(final long assetId) {
        final List<String> differences = new ArrayList<String>();
        final AssetConfiguration stored = assetConfigDAO.getCurrentConfiguration(assetId);
        final AssetConfiguration reported =
                modifyConfigurationDAO.getDeviceReportedConfiguration(assetId);
        if (stored == null || reported == null) {
            return differences;
        }
        addDifference(differences, "LAN IP address", stored.getLanIpAddress(),
                reported.getLanIpAddress());
        addDifference(differences, "LAN subnet mask", stored.getLanSubnetMask(),
                reported.getLanSubnetMask());
        addDifference(differences, "WAN IP address", stored.getWanIpAddress(),
                reported.getWanIpAddress());
        addDifference(differences, "WAN subnet mask", stored.getWanSubnetMask(),
                reported.getWanSubnetMask());
        addDifference(differences, "Default gateway", stored.getDefaultGateway(),
                reported.getDefaultGateway());
        addDifference(differences, "Primary DNS", stored.getPrimaryDnsAddress(),
                reported.getPrimaryDnsAddress());
        addDifference(differences, "Secondary DNS", stored.getSecondaryDnsAddress(),
                reported.getSecondaryDnsAddress());
        addDifference(differences, "Circuit id", stored.getCircuitId(), reported.getCircuitId());
        return differences;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void acceptDeviceConfiguration(final long assetId, final String userId) {
        final AssetConfiguration stored = assetConfigDAO.getCurrentConfiguration(assetId);
        final AssetConfiguration reported =
                modifyConfigurationDAO.getDeviceReportedConfiguration(assetId);
        if (stored == null || reported == null) {
            return;
        }
        reported.setAssetId(Long.valueOf(assetId));
        assetConfigDAO.markSuperseded(stored.getConfigurationId().longValue(), userId);
        assetConfigDAO.insertConfigurationRevision(reported, userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void reapplyStoredConfiguration(final long assetId, final String userId) {
        final AssetConfiguration stored = assetConfigDAO.getCurrentConfiguration(assetId);
        if (stored != null && stored.getConfigurationId() != null) {
            modifyConfigurationDAO.resolveMismatch(stored.getConfigurationId().longValue(), userId);
        }
    }

    private static void addDifference(final List<String> differences, final String label,
            final String stored, final String reported) {
        if (stored == null ? reported == null : stored.equals(reported)) {
            return;
        }
        differences.add(label + ": AMS has '" + stored + "', device reports '" + reported + "'");
    }
}
