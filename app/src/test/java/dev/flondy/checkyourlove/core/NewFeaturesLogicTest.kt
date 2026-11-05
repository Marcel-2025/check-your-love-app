package dev.flondy.checkyourlove.core

import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.content.Content
import dev.flondy.checkyourlove.content.IdeaCategory
import dev.flondy.checkyourlove.content.QuizPack
import dev.flondy.checkyourlove.ui.group.GroupViewModel
import dev.flondy.checkyourlove.ui.quiz.QuizPhase
import dev.flondy.checkyourlove.ui.quiz.QuizViewModel
import org.junit.Test
import kotlin.random.Random

class NewFeaturesLogicTest {

    @Test fun loveResultUnchangedByNewModeParameter() {
        // Bestehende Ergebnisse dürfen sich durch Version 2.1 nicht ändern.
        assertThat(LoveCalculator.calculate("Anna", "Ben").percent).isEqualTo(79)
        assertThat(LoveCalculator.calculate("Anna", "Ben", TestMode.LOVE)).isEqualTo(LoveCalculator.calculate("Anna", "Ben"))
    }

    @Test fun friendshipIsDeterministicOrderIndependentAndStoredSeparately() {
        val a = LoveCalculator.calculate("Lena", "Mia", TestMode.FRIENDSHIP)
        val b = LoveCalculator.calculate("mia", " Lena ", TestMode.FRIENDSHIP)
        assertThat(a).isEqualTo(b)
        assertThat(a.pairKey).startsWith(TestMode.FRIEND_PREFIX)
        assertThat(TestMode.ofPairKey(a.pairKey)).isEqualTo(TestMode.FRIENDSHIP)
        assertThat(TestMode.ofPairKey(LoveCalculator.calculate("Lena", "Mia").pairKey)).isEqualTo(TestMode.LOVE)
    }

    @Test fun groupContainsAllPairsSortedAndMatchesSingleTest() {
        val pairs = GroupCalculator.pairs(listOf("Anna", "Ben", "Lena", "Tom"), TestMode.LOVE)
        assertThat(pairs).hasSize(6)
        assertThat(pairs.map { it.calculation.percent }).isInOrder(Comparator.reverseOrder<Int>())
        val annaBen = pairs.first { setOf(it.nameA, it.nameB) == setOf("Anna", "Ben") }
        assertThat(annaBen.calculation.percent).isEqualTo(79)
    }

    @Test fun groupRejectsDuplicatesAndTooFewNames() {
        val vm = GroupViewModel()
        vm.setName(0, "Anna"); vm.setName(1, "anna "); vm.setName(2, "Ben")
        assertThat(vm.evaluate()).isFalse()
        assertThat(vm.state.value.errors.keys).contains(1)
        vm.setName(1, "")
        assertThat(vm.evaluate()).isFalse()
        assertThat(vm.state.value.tooFew).isTrue()
        vm.setName(1, "Lena")
        assertThat(vm.evaluate()).isTrue()
        assertThat(vm.state.value.results).hasSize(3)
    }

    @Test fun groupLimitsNameCount() {
        val vm = GroupViewModel()
        repeat(10) { vm.add() }
        assertThat(vm.state.value.names).hasSize(GroupCalculator.MAX_NAMES)
        repeat(10) { vm.remove(0) }
        assertThat(vm.state.value.names).hasSize(GroupCalculator.MIN_NAMES)
    }

    @Test fun quizRoundScoresCorrectGuessesAndAlternatesRoles() {
        val vm = QuizViewModel(Random(1))
        vm.setNameA("Anna"); vm.setNameB("Ben")
        assertThat(vm.start(Content.quiz(QuizPack.BASIC, german = true))).isTrue()
        assertThat(vm.state.value.questions).hasSize(QuizEngine.QUESTIONS_PER_ROUND)
        assertThat(vm.state.value.questions.map { it.id }.toSet()).hasSize(QuizEngine.QUESTIONS_PER_ROUND)
        repeat(QuizEngine.QUESTIONS_PER_ROUND) { i ->
            val s = vm.state.value
            assertThat(s.answererName).isEqualTo(if (i % 2 == 0) "Anna" else "Ben")
            vm.answer(2); vm.ready()
            vm.guess(if (i < 7) 2 else 0) // 7 richtig, 3 falsch
            assertThat(vm.state.value.phase).isEqualTo(QuizPhase.REVEAL)
            vm.next()
        }
        assertThat(vm.state.value.phase).isEqualTo(QuizPhase.RESULT)
        assertThat(vm.state.value.correct).isEqualTo(7)
        assertThat(vm.state.value.score).isEqualTo(70)
    }

    @Test fun quizRequiresTwoDifferentValidNames() {
        val vm = QuizViewModel(Random(1))
        vm.setNameA("Anna"); vm.setNameB(" anna")
        assertThat(vm.start(Content.quiz(QuizPack.BASIC, german = true))).isFalse()
        assertThat(vm.state.value.sameNames).isTrue()
    }

    @Test fun contentIsCompleteInBothLanguages() {
        QuizPack.entries.forEach { pack ->
            val de = Content.quiz(pack, german = true); val en = Content.quiz(pack, german = false)
            assertThat(de.size).isEqualTo(en.size)
            assertThat(de.size).isAtLeast(QuizEngine.QUESTIONS_PER_ROUND)
            (de + en).forEach { assertThat(it.options).hasSize(4) }
        }
        IdeaCategory.entries.forEach { cat ->
            assertThat(Content.ideas(cat, true).size).isEqualTo(Content.ideas(cat, false).size)
            assertThat(Content.ideas(cat, true)).isNotEmpty()
        }
        assertThat(Content.idea("flirt:3", true)?.category).isEqualTo(IdeaCategory.FLIRT)
        assertThat(Content.idea("unknown:1", true)).isNull()
    }

    @Test fun premiumContentIsMarkedButBasicIsFree() {
        assertThat(QuizPack.BASIC.premium).isFalse()
        assertThat(QuizPack.entries.count { it.premium }).isEqualTo(2)
        assertThat(IdeaCategory.entries.count { !it.premium }).isEqualTo(4)
    }
}
