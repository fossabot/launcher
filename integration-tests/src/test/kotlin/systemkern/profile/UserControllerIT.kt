package systemkern.profile

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import systemkern.CliEntryPoint
import systemkern.IntegrationTest
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [CliEntryPoint::class])
internal class UserControllerIT : IntegrationTest() {

    private val restUrl = "/user-profiles"

    private val entityRequestFields = listOf(

        fieldWithPath("name").description("Name of the user").type(STRING),
        fieldWithPath("username").description("Username of the user").type(STRING),
        fieldWithPath("password").description("Password of user to be created").type(STRING)
    )
    private val entityResponseFields = listOf(
        fieldWithPath("id").description("The Id of the user entity").type(STRING),
        fieldWithPath("name").description("Name of the user").type(STRING),
        fieldWithPath("username").description("Username to log in").type(STRING),
        fieldWithPath("_links.self.href").description("Link to access the created user").type(STRING),
        fieldWithPath("_links.userProfile.href").description("Link to access the created user").type(STRING)
    )

    @Autowired
    private lateinit var testDataCreator: UserProfileTestDataCreator
    private lateinit var userId: UUID

    @Before
    fun setup() {
        testDataCreator.persistTestData()
        this.userId = testDataCreator.userId
    }

    @Test
    fun `Can create a User`() {
        this.mockMvc.perform(RestDocumentationRequestBuilders.post(restUrl)
            .content(objectMapper.writeValueAsString(
                TestUser(
                    username = "AndresAusecha09",
                    name = "Andres Ausecha",
                    password = "AndresAusecha09*"
                )
            ))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isCreated)
            .andDo(document("user_create",
                requestFields(entityRequestFields),
                responseFields(entityResponseFields)
            ))
    }

    @Test
    fun `Can read User`() {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("$restUrl/$userId")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andDo(document("user_read",
                responseFields(entityResponseFields)
            ))
    }

    @Test
    fun `Can update User`() {
        this.mockMvc.perform(RestDocumentationRequestBuilders.put("$restUrl/$userId")
            .content(
                objectMapper.writeValueAsString(
                TestUser(
                    username = "Test user to update",
                    name = "Test user to update",
                    password = "TestUserUpdate2018"
                )
            ))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk)
            .andDo(document("user_update",
                requestFields(entityRequestFields),
                responseFields(entityResponseFields)
            ))
    }

    @Test
    fun `Can delete User`() {
        this.mockMvc.perform(RestDocumentationRequestBuilders.delete("$restUrl/$userId")
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isNoContent)
            .andDo(document("user_delete"))
    }
}

private data class TestUser(
    val username: String,
    val name: String,
    val password: String
)
