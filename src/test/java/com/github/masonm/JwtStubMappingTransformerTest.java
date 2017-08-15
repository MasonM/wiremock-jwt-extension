package com.github.masonm;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JwtStubMappingTransformerTest {
    private static final JwtStubMappingTransformer TRANSFORMER = new JwtStubMappingTransformer();
    private static final StubMapping TEST_MAPPING = WireMock.get("/").build();
    private static final String TEST_MAPPING_JSON = TEST_MAPPING.toString();

    @Test
    public void returnsMappingUnmodifiedWithMissingParameters() {
        TRANSFORMER.transform(TEST_MAPPING, null, Parameters.empty());
        assertThat(TEST_MAPPING.toString(), is(TEST_MAPPING_JSON));
    }
}
