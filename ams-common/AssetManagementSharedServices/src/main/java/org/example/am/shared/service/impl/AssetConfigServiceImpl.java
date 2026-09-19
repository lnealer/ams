package org.example.am.shared.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.example.am.network.validation.LanTypeAValidator;
import org.example.am.network.validation.LanTypeBValidator;
import org.example.am.network.validation.LanTypeCValidator;
import org.example.am.network.validation.LanValidator;
import org.example.am.network.validation.WanValidator;
import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.AssetConfigurationType;
import org.example.am.shared.service.AssetConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and writes asset configuration revisions, validating the network settings before a
 * revision is stored.
 */
@Service("assetConfigService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AssetConfigServiceImpl implements AssetConfigService {

    @Autowired
    private AssetConfigDAO assetConfigDAO;

    @Override
    public AssetConfiguration getCurrentConfiguration(final long assetId) {
        return assetConfigDAO.getCurrentConfiguration(assetId);
    }

    @Override
    public List<AssetConfiguration> getHistory(final long assetId) {
        return assetConfigDAO.getConfigurationHistory(assetId);
    }

    @Override
    public List<String> validate(final AssetConfiguration configuration) {
        final List<String> messages = new ArrayList<String>();
        if (configuration == null) {
            messages.add("No configuration supplied.");
            return messages;
        }
        final LanValidator lanValidator = getLanValidator(configuration.getAssetConfigurationType());
        if (lanValidator != null) {
            messages.addAll(lanValidator.validate(configuration.getLanIpAddress(),
                    configuration.getLanSubnetMask(), configuration.getDefaultGateway()));
        }
        if (configuration.getWanIpAddress() != null) {
            messages.addAll(new WanValidator().validate(configuration.getWanIpAddress(),
                    configuration.getWanSubnetMask(), configuration.getDefaultGateway(),
                    configuration.getPrimaryDnsAddress(), configuration.getSecondaryDnsAddress()));
        }
        return messages;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long saveRevision(final AssetConfiguration configuration, final String userId) {
        final List<String> messages = validate(configuration);
        if (!messages.isEmpty()) {
            throw new IllegalArgumentException("Configuration is not valid: " + messages);
        }
        return assetConfigDAO.insertConfigurationRevision(configuration, userId);
    }

    /**
     * @return the validator for this LAN shape, or {@code null} for configurations that have no LAN
     *         side at all
     */
    private static LanValidator getLanValidator(final AssetConfigurationType type) {
        if (AssetConfigurationType.LAN_TYPE_A.equals(type)) {
            return new LanTypeAValidator();
        }
        if (AssetConfigurationType.LAN_TYPE_B.equals(type)) {
            return new LanTypeBValidator();
        }
        if (AssetConfigurationType.LAN_TYPE_C.equals(type)) {
            return new LanTypeCValidator();
        }
        return null;
    }
}
