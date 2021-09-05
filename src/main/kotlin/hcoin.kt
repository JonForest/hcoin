package hcoin

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant

fun String.hash(algorithm: String = "SHA-256"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(this.toByteArray())
    return String.format("%064x", BigInteger(1, messageDigest.digest()))
}

data class Block(
    val previousHash: String,
    val data: String,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val nonce: Long = 0,
    var hash: String = ""
) {
    init {
        hash = calculateHash()
    }

    fun calculateHash(): String {
        return "$previousHash$data$timestamp$nonce".hash()
    }
}

class BlockChain {
    private var blocks: MutableList<Block> = mutableListOf()
    private val difficulty = 5
    private val validPrefix = "0".repeat(difficulty)

    fun add(block: Block): Block {
        val minedBlock = if (isMined(block)) block else mine(block)
        blocks.add(minedBlock)
        return minedBlock
    }

    fun isValid(): Boolean {
        when {
            blocks.isEmpty() -> return true
            blocks.size == 1 -> return blocks[0].hash == blocks[0].calculateHash()
            else -> {
                for (i in 1 until blocks.size) {
                    val previousBlock = blocks[i-1]
                    val currentBlock = blocks[i]

                    when {
                        currentBlock.hash != currentBlock.calculateHash() -> return false
                        currentBlock.previousHash != previousBlock.calculateHash() -> return false
                        !(isMined(previousBlock) && isMined(currentBlock)) -> return false
                    }
                }
                return true
            }
        }
    }

    private fun isMined(block: Block): Boolean {
        return block.hash.startsWith(validPrefix)
    }

    private fun mine(block: Block): Block {
        println("Mining: $block")

        var minedBlock = block.copy()
        while (!isMined(minedBlock)) {
            minedBlock = minedBlock.copy(nonce = minedBlock.nonce + 1)
        }

        println("Mined: $minedBlock")

        return minedBlock
    }


}

fun main(args: Array<String>) {
    val blockChain = BlockChain()
    val genesisBlock = blockChain.add(Block("0", "I'm the first"))
    val secondBlock = blockChain.add(Block(genesisBlock.hash, "I'm the second"))
    val thirdBlock = blockChain.add(Block(secondBlock.hash, "I'm the third"))

    println(genesisBlock)
    println(secondBlock)
    println(thirdBlock)
}