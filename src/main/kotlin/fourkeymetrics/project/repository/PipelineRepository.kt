package fourkeymetrics.project.repository

import fourkeymetrics.common.encryption.AESEncryptionService
import fourkeymetrics.project.exception.PipelineNotFoundException
import fourkeymetrics.project.model.Pipeline
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
class PipelineRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var encryptionService: AESEncryptionService

    fun findById(pipelineId: String): Pipeline {
        val query = Query().addCriteria(
            Criteria.where("id").isEqualTo(pipelineId)
        )

        return mongoTemplate.findOne(query, Pipeline::class.java) ?: throw PipelineNotFoundException()
    }

    fun findByIdAndProjectId(pipelineId: String, projectId: String): Pipeline {
        val query = Query().addCriteria(
            Criteria.where("id").isEqualTo(pipelineId).and("projectId").isEqualTo(projectId)
        )

        return mongoTemplate.findOne(query, Pipeline::class.java) ?: throw PipelineNotFoundException()
    }

    fun findByNameAndProjectId(name: String, projectId: String): Pipeline? {
        val query = Query().addCriteria(Criteria.where("name").`is`(name).and("projectId").`is`(projectId))

        return mongoTemplate.findOne(query, Pipeline::class.java)
    }

    fun findByProjectId(projectId: String): List<Pipeline> {
        val query = Query().addCriteria(Criteria.where("projectId").isEqualTo(projectId))

        return mongoTemplate.find(query, Pipeline::class.java)
    }

    fun deleteById(pipelineId: String) {
        val query = Query().addCriteria(Criteria.where("id").isEqualTo(pipelineId))
        mongoTemplate.remove(query, Pipeline::class.java)
    }

    fun save(pipeline: Pipeline): Pipeline {
        pipeline.username = encryptionService.encrypt(pipeline.username)
        pipeline.credential = encryptionService.encrypt(pipeline.credential)
        return mongoTemplate.save(pipeline)
    }

    fun saveAll(pipelines: List<Pipeline>): List<Pipeline> {
        pipelines.forEach {
            it.username = encryptionService.encrypt(it.username)
            it.credential = encryptionService.encrypt(it.credential)
        }
        return mongoTemplate.insert(pipelines.toMutableList(), Pipeline::class.java).toList()
    }
}
