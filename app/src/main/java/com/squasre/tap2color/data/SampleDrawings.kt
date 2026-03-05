package com.squasre.tap2color.data

data class DrawingTemplate(
    val id: String,
    val name: String,
    val svgContent: String
)

object SampleDrawings {
    val cat = DrawingTemplate(
        id = "cat",
        name = "Cat",
        svgContent = """
            <svg viewBox="0 0 300 300">
            <path id="face"
            d="M150 60
            C80 60 50 120 50 170
            C50 230 100 260 150 260
            C200 260 250 230 250 170
            C250 120 220 60 150 60Z"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            <path id="ear_left"
            d="M80 80 L120 20 L140 80 Z"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            <path id="ear_right"
            d="M160 80 L180 20 L220 80 Z"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            </svg>
        """.trimIndent()
    )

    val apple = DrawingTemplate(
        id = "apple",
        name = "Apple",
        svgContent = """
            <svg viewBox="0 0 300 300">
            <path id="apple_body"
            d="M150 90
            C80 90 60 150 60 190
            C60 240 100 260 150 260
            C200 260 240 240 240 190
            C240 150 220 90 150 90Z"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            <path id="leaf"
            d="M150 70 C170 40 210 40 200 80 Z"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            </svg>
        """.trimIndent()
    )

    val car = DrawingTemplate(
        id = "car",
        name = "Car",
        svgContent = """
            <svg viewBox="0 0 400 200">
            <rect id="car_body"
            x="60" y="80"
            width="280"
            height="80"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            <circle id="wheel_left"
            cx="120"
            cy="170"
            r="30"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            <circle id="wheel_right"
            cx="280"
            cy="170"
            r="30"
            fill="#FFFFFF"
            stroke="#000"
            stroke-width="4"/>
            </svg>
        """.trimIndent()
    )

    val all = listOf(cat, apple, car)
}
