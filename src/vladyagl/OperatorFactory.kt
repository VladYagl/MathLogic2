package vladyagl

interface OperatorFactory <out T : Expression> {
    val name : String
    val leftPriority: Boolean
    val size: Int
    val symbol: String
    abstract fun create(vararg args: Expression): T
}