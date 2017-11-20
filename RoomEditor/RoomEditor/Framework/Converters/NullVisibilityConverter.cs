using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace RoomEditor.Framework.Converters
{
    public class NullVisibilityConverter : IValueConverter
    {
        public Visibility TrueValue { get; set; } = Visibility.Visible;
        public Visibility FalseValue { get; set; } = Visibility.Collapsed;

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            return (value is bool b && b) ? TrueValue : FalseValue;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            return (value is Visibility visibility && visibility == TrueValue);
        }
    }
}
