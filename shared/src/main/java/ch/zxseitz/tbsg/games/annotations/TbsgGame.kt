package ch.zxseitz.tbsg.games.annotations;

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TbsgGame(val name: String, val colors: Array<Color>)
