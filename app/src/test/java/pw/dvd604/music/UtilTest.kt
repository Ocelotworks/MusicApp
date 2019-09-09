package pw.dvd604.music

import org.hamcrest.core.IsNull
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.internal.matchers.Not
import org.robolectric.RobolectricTestRunner
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.Util
import org.hamcrest.CoreMatchers.`is` as Is

@RunWith(RobolectricTestRunner::class)
class UtilTest {

    private fun getSong(): Media {
        return Media().fromJson(JSONObject("{\"song_id\":\"c1190133-1e6b-468f-8c3c-ce1bef49adc6\",\"artist_id\":\"db7411ae-14c3-4029-aa25-ef306db6d47b\",\"name\":\"test\",\"title\":\"test\",\"album\":\"43c788b3-7342-4c38-ab17-66846cfa4255\",\"path\":\"test\",\"genre\":\"1234567891234567\",\"hash\":\"abcdef\"}"))
    }

    @Test
    fun prettySeconds_CorrectTime() {
        val time = Util.prettyTime(30 as Int)
        assertThat(time, Is("00:30"))
    }

    @Test
    fun prettySeconds_IncorrectTime() {
        val time = Util.prettyTime(60.toLong())
        assertThat(time, Not(Is("00:30")))
    }

    @Test
    fun jsonToSong_CorrectValue() {
        val song = getSong()
        assertThat(song.name, Is("test"))
    }

    @Test
    fun songToJson_jsonSongJson_CorrectValue() {
        val song = getSong()
        val json = song.toJson()

        assertThat(json, Is(IsNull.notNullValue()))

        assertThat(song.name, Is(json.getString("name")))

        val newSong = Media().fromJson(json)

        assertThat(newSong.name, Is(song.name))
    }
}