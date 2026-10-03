import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
} from 'react-native';
import { getDaysInMonth, format, addMonths, startOfMonth, isToday, isSameDay } from 'date-fns';

interface CalendarPickerProps {
  selectedDate?: Date;
  onDateSelect: (date: Date) => void;
  availableDates?: Date[];
  minDate?: Date;
  maxDate?: Date;
}

const CalendarPicker: React.FC<CalendarPickerProps> = ({
  selectedDate,
  onDateSelect,
  availableDates = [],
  minDate,
  maxDate,
}) => {
  const [currentMonth, setCurrentMonth] = React.useState(new Date());

  const monthStart = startOfMonth(currentMonth);
  const daysInMonth = getDaysInMonth(monthStart);
  const startingDayOfWeek = monthStart.getDay();

  const days: (Date | null)[] = [];
  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push(null);
  }
  for (let i = 1; i <= daysInMonth; i++) {
    days.push(new Date(currentMonth.getFullYear(), currentMonth.getMonth(), i));
  }

  const isDateAvailable = (date: Date): boolean => {
    if (!date) return false;
    if (minDate && date < minDate) return false;
    if (maxDate && date > maxDate) return false;
    if (availableDates.length === 0) return true;
    return availableDates.some(d => isSameDay(d, date));
  };

  const isDateSelectable = (date: Date): boolean => {
    return isDateAvailable(date) && date >= new Date(new Date().setHours(0, 0, 0, 0));
  };

  const handlePrevMonth = () => {
    setCurrentMonth(addMonths(currentMonth, -1));
  };

  const handleNextMonth = () => {
    setCurrentMonth(addMonths(currentMonth, 1));
  };

  const dayHeaders = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={handlePrevMonth} style={styles.navButton}>
          <Text style={styles.navButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.monthYear}>
          {format(currentMonth, 'MMMM yyyy')}
        </Text>
        <TouchableOpacity onPress={handleNextMonth} style={styles.navButton}>
          <Text style={styles.navButtonText}>→</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.dayHeaders}>
        {dayHeaders.map((day) => (
          <Text key={day} style={styles.dayHeader}>
            {day}
          </Text>
        ))}
      </View>

      <View style={styles.grid}>
        {days.map((date, index) => (
          <TouchableOpacity
            key={index}
            style={[
              styles.dayCell,
              !date && styles.emptyCell,
              date && !isDateSelectable(date) && styles.disabledCell,
              date &&
                selectedDate &&
                isSameDay(date, selectedDate) &&
                styles.selectedCell,
            ]}
            onPress={() => {
              if (date && isDateSelectable(date)) {
                onDateSelect(date);
              }
            }}
            disabled={!date || !isDateSelectable(date)}
          >
            {date && (
              <Text
                style={[
                  styles.dayText,
                  !isDateSelectable(date) && styles.disabledText,
                  selectedDate &&
                    isSameDay(date, selectedDate) &&
                    styles.selectedText,
                  isToday(date) && styles.todayText,
                ]}
              >
                {format(date, 'd')}
              </Text>
            )}
            {date && isToday(date) && (
              <View style={styles.todayIndicator} />
            )}
          </TouchableOpacity>
        ))}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginHorizontal: 20,
    marginVertical: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  navButton: {
    padding: 8,
  },
  navButtonText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#6B4C9A',
  },
  monthYear: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  dayHeaders: {
    flexDirection: 'row',
    marginBottom: 8,
  },
  dayHeader: {
    flex: 1,
    textAlign: 'center',
    fontSize: 12,
    fontWeight: '600',
    color: '#999',
    paddingVertical: 4,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  dayCell: {
    width: '14.28%',
    aspectRatio: 1,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    margin: 2,
    position: 'relative',
  },
  emptyCell: {
    backgroundColor: 'transparent',
  },
  disabledCell: {
    backgroundColor: '#f5f5f5',
  },
  selectedCell: {
    backgroundColor: '#6B4C9A',
  },
  dayText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#333',
  },
  disabledText: {
    color: '#ccc',
  },
  selectedText: {
    color: '#fff',
    fontWeight: '700',
  },
  todayText: {
    color: '#FF9500',
    fontWeight: '700',
  },
  todayIndicator: {
    position: 'absolute',
    bottom: 4,
    width: 4,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#FF9500',
  },
});

export default CalendarPicker;
